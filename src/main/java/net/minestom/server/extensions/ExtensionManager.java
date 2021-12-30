package net.minestom.server.extensions;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.utils.PropertyUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ExtensionManager {
    public final static Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    // Properties
    private static final boolean LOAD_ON_START = PropertyUtils.getBoolean("minestom.extension.load-on-start", true);

    // Server state
    private final ExceptionManager exceptionManager;
    private final EventNode<Event> globalEventNode;
    private final List<ExtensionDiscoverer> discoverers;

    // Internal state
    private final Map<String, Extension> extensions = new LinkedHashMap<>();
    private final Map<String, Extension> immutableExtensions = Collections.unmodifiableMap(extensions);
    private final Map<String, HierarchyClassLoader> externalDependencies = new HashMap<>();

    private Path extensionDataRoot = Paths.get("extensions");
    private Path dependenciesFolder = extensionDataRoot.resolve(".libs");

    private boolean started = false;

    public ExtensionManager(ExceptionManager exceptionManager, EventNode<Event> globalEventNode) {
        this(exceptionManager, globalEventNode, List.of(
                ExtensionDiscoverer.FILESYSTEM, ExtensionDiscoverer.INDEV, ExtensionDiscoverer.AUTOSCAN));
    }

    ExtensionManager(ExceptionManager exceptionManager, EventNode<Event> globalEventNode, List<ExtensionDiscoverer> discoverer) {
        this.exceptionManager = exceptionManager;
        this.globalEventNode = globalEventNode;
        this.discoverers = new ArrayList<>(discoverer);
    }

    public @NotNull Path getExtensionDataRoot() {
        return extensionDataRoot;
    }

    public void setExtensionDataRoot(@NotNull Path dataRoot) {
        Check.stateCondition(started, "Cannot set extension data root after initialization.");
        this.extensionDataRoot = dataRoot;
        this.dependenciesFolder = extensionDataRoot.resolve(".libs");
    }

    public void addExtensionDiscoverer(@NotNull ExtensionDiscoverer discoverer) {
        Check.stateCondition(started, "Cannot add extension discoverer after initialization.");
        discoverers.add(discoverer);
    }

    public void clearExtensionDiscoverers() {
        Check.stateCondition(started, "Cannot clear extension discoverers after initialization.");
        discoverers.clear();
    }

    @NotNull
    public Collection<Extension> getExtensions() {
        return immutableExtensions.values();
    }

    @Nullable
    public Extension getExtension(@NotNull String name) {
        return extensions.get(name.toLowerCase());
    }

    public boolean hasExtension(@NotNull String name) {
        return extensions.containsKey(name);
    }

    @ApiStatus.Internal
    public void start() {
        Check.stateCondition(started, "ExtensionManager has already been initialized");
        if (!LOAD_ON_START) return;

        started = true;

        try {
            if (!Files.exists(extensionDataRoot)) {
                Files.createDirectories(extensionDataRoot);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create extension directory!", e);
            return;
        }

        // Discover all extensions
        Map<String, ExtensionDescriptor> extensionByName = discoverers.stream()
                .flatMap(discoverer -> discoverer.discover(extensionDataRoot))
                .collect(Collectors.toMap(ext -> ext.name().toLowerCase(), Function.identity()));
        if (extensionByName.isEmpty()) return;

        // Compute the load order depending on dependencies
        List<ExtensionDescriptor> loadOrder = computeLoadOrder(extensionByName);

        // initialize
        for (ExtensionDescriptor ext : loadOrder) {
            if (extensions.containsKey(ext.name().toLowerCase()))
                continue; // Already loaded

            loadExtension(ext, extensionByName);
        }
    }

    @ApiStatus.Internal
    public void shutdown() {
        // Shutdown in reverse order, so that dependencies are unloaded last
        List<Extension> reverseLoadOrder = new ArrayList<>(extensions.values());
        Collections.reverse(reverseLoadOrder);

        for (Extension ext : reverseLoadOrder) {
            unloadExtension(ext);
        }
    }

    //
    // Loading
    //

    List<ExtensionDescriptor> computeLoadOrder(Map<String, ExtensionDescriptor> extensionsByName) {
        Map<ExtensionDescriptor, List<ExtensionDescriptor>> dependents = new HashMap<>();
        for (ExtensionDescriptor extension : extensionsByName.values()) {
            for (Dependency dependency : extension.dependencies()) {
                if (dependency instanceof Dependency.ExtensionDependency extensionDependency) {
                    ExtensionDescriptor dependencyExtension = extensionsByName.get(extensionDependency.id().toLowerCase());
                    if (dependencyExtension == null) {
                        if (extensionDependency.isOptional()) {
                            LOGGER.debug("Optional extension {} (for {}) was not found.", extensionDependency.id(), extension.name());
                        } else {
                            throw new IllegalStateException("Unknown extension: " + extensionDependency.id() + " (dependency of " + extension.name() + ")");
                        }
                    }
                    dependents.computeIfAbsent(dependencyExtension, k -> new ArrayList<>())
                            .add(extension);
                }
            }
        }

        List<ExtensionDescriptor> ordered = new ArrayList<>();
        for (ExtensionDescriptor extension : extensionsByName.values()) {
            if (ordered.contains(extension)) continue;

            computeLoadOrderRecursive(extension, ordered, dependents, List.of());
        }

        return ordered;
    }

    private void computeLoadOrderRecursive(ExtensionDescriptor target, List<ExtensionDescriptor> loadOrder, Map<ExtensionDescriptor, List<ExtensionDescriptor>> dependentMap, List<String> path) {
        if (loadOrder.contains(target)) return;
        if (path.contains(target.name())) {
            throw new IllegalStateException("Illegal circular extension dependency: " + String.join(" -> ", path) + " -> " + target.name());
        }

        List<String> newPath = new ArrayList<>(path);
        newPath.add(target.name());

        for (ExtensionDescriptor dependent : dependentMap.getOrDefault(target, Collections.emptyList())) {
            computeLoadOrderRecursive(dependent, loadOrder, dependentMap, newPath);
        }

        loadOrder.add(0, target);
    }

    boolean loadExtension(ExtensionDescriptor extension, Map<String, ExtensionDescriptor> extensionsById) {
        // Do not load if it is already loaded
        //TODO this creates an issue. If an extension fails to load (initialize = FAILED) then we
        if (extensions.containsKey(extension.name().toLowerCase()))
            return true;

        // Load dependencies
        for (Dependency dependency : extension.dependencies()) {
            boolean loaded = loadDependency(extension, dependency, extensionsById);
            if (!loaded) {
                LOGGER.error("Failed to load {}, dependency {} could was not loaded.", extension.name(), dependency.id());
                return false;
            }
        }

        // Load extension
        Extension extensionInstance = createExtensionImpl(extension);
        if (extensionInstance == null)
            return false;

        // initialize
        Extension.LoadStatus result = Extension.LoadStatus.FAILED;
        try {
            result = extensionInstance.initialize();
        } catch (Throwable throwable) {
            LOGGER.error("An exception occurred while initializing extension {}", extension.name(), throwable);
        }
        if (result == Extension.LoadStatus.FAILED) {
            LOGGER.error("Failed to initialize extension {}, it nor its dependents will be loaded.", extension.name());
            return false;
        }

        extensions.put(extension.name().toLowerCase(), extensionInstance);
        return true;
    }

    private boolean loadDependency(ExtensionDescriptor target, Dependency dep, Map<String, ExtensionDescriptor> extensionsById) {
        // Load child and get classloader
        HierarchyClassLoader dependencyClassLoader = null;
        if (dep instanceof Dependency.ExtensionDependency dependency) {
            ExtensionDescriptor descriptor = extensionsById.get(dependency.id().toLowerCase());
            //todo what happens if extension does not exist?
            boolean loaded = loadExtension(descriptor, extensionsById);
            if (!loaded) return false;
            dependencyClassLoader = descriptor.classLoader();
        } else if (dep instanceof Dependency.MavenDependency dependency) {
            Check.fail("Not implemented");
        }

        // Add classloader to target
        Check.stateCondition(dependencyClassLoader == null, "Something went wrong while loading a dependency. (extension={0},dependency={1})", target, dep);
        target.classLoader().addChild(dependencyClassLoader);
        return true;
    }

    Extension createExtensionImpl(ExtensionDescriptor descriptor) {
        String extensionName = descriptor.name();
        String mainClassName = descriptor.entrypoint();
        HierarchyClassLoader loader = descriptor.classLoader();

        // Somewhat unnecessary, but better to be safe.
        Check.stateCondition(extensions.containsKey(extensionName.toLowerCase()),
                "Extension '{}' is already loaded.", extensionName);

        Class<? extends Extension> extensionClass;
        try {
            Class<?> mainClass = Class.forName(mainClassName, true, loader);
            extensionClass = mainClass.asSubclass(Extension.class);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Unable to find or load main class '{}' in extension '{}'.",
                    mainClassName, extensionName, e);
            return null;
        } catch (ClassCastException e) {
            LOGGER.error("Main class '{}' in '{}' does not extend 'Extension'.",
                    mainClassName, extensionName, e);
            return null;
        }

        Extension extension = null;
        try {
            Constructor<? extends Extension> constructor = extensionClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            extension = constructor.newInstance();
        } catch (NoSuchMethodException e) {
            LOGGER.error("Main class '{}' in '{}' does not define a no-args constructor.",
                    mainClassName, extensionName, e);
            return null;
        } catch (InstantiationException e) {
            LOGGER.error("Main class '{}' in '{}' cannot be an abstract class.",
                    mainClassName, extensionName, e);
            return null;
        } catch (IllegalAccessException ignored) {
            // We made it accessible, should not occur
            return null;
        } catch (InvocationTargetException e) {
            LOGGER.error("An exception was thrown while instantiating main class '{}' in '{}'",
                    mainClassName, extensionName, e.getTargetException());
            return null;
        }

        // Set extension descriptor, logger, and event node
        extension.descriptor = descriptor;
        extension.logger = LoggerFactory.getLogger(extension.getClass());
        EventNode<Event> eventNode = EventNode.all(extensionName);
        globalEventNode.addChild(eventNode);
        extension.eventNode = eventNode;

        return extension;
    }

//    private void loadDependencies(@NotNull List<DiscoveredExtension> extensions) {
//        for (DiscoveredExtension discoveredExtension : extensions) {
//            try {
//                DependencyGetter getter = new DependencyGetter();
//                DiscoveredExtension.ExternalDependencies externalDependencies = discoveredExtension.externalDependencies();
//                List<MavenRepository> repoList = new ArrayList<>();
//                for (var repository : externalDependencies.repositories()) {
//                    Check.stateCondition(repository.name().isEmpty(), "Missing 'name' element in repository object.");
//                    Check.stateCondition(repository.url().isEmpty(), "Missing 'url' element in repository object.");
//
//                    repoList.add(new MavenRepository(repository.name(), repository.url()));
//                }
//
//                getter.addMavenResolver(repoList);
//
//                for (String artifact : externalDependencies.artifacts()) {
//                    var resolved = getter.get(artifact, dependenciesFolder.toFile());
//                    addDependencyFile(resolved, discoveredExtension);
//                    LOGGER.trace("Dependency of extension {}: {}", discoveredExtension.name(), resolved);
//                }
//
//                HierarchyClassLoader extensionClassLoader = discoveredExtension.classLoader();
//                for (String dependencyName : discoveredExtension.dependencies()) {
//                    var resolved = extensions.stream()
//                            .filter(ext -> ext.name().equalsIgnoreCase(dependencyName))
//                            .findFirst()
//                            .orElseThrow(() -> new IllegalStateException("Unknown dependency '" + dependencyName + "' of '" + discoveredExtension.name() + "'"));
//
//                    HierarchyClassLoader dependencyClassLoader = resolved.classLoader();
//
//                    extensionClassLoader.addChild(dependencyClassLoader);
//                    LOGGER.trace("Dependency of extension {}: {}", discoveredExtension.name(), resolved);
//                }
//            } catch (Exception e) {
//                discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.MISSING_DEPENDENCIES;
//                LOGGER.error("Failed to load dependencies for extension {}", discoveredExtension.name());
//                LOGGER.error("Extension '{}' will not be loaded", discoveredExtension.name());
//                LOGGER.error("This is the exception", e);
//            }
//        }
//    }
//
//    private void addDependencyFile(@NotNull ResolvedDependency dependency, @NotNull DiscoveredExtension extension) {
//        URL location = dependency.getContentsLocation();
//        extension.files.add(location);
//        extension.classLoader().addURL(location);
//        LOGGER.trace("Added dependency {} to extension {} classpath", location.toExternalForm(), extension.name());
//
//        // recurse to add full dependency tree
//        if (!dependency.getSubdependencies().isEmpty()) {
//            LOGGER.trace("Dependency {} has subdependencies, adding...", location.toExternalForm());
//            for (ResolvedDependency sub : dependency.getSubdependencies()) {
//                addDependencyFile(sub, extension);
//            }
//            LOGGER.trace("Dependency {} has had its subdependencies added.", location.toExternalForm());
//        }
//    }

    //
    // Extension unloading
    //

    private void unloadExtension(Extension extension) {
        String extensionName = extension.descriptor().name();

        Check.stateCondition(extensions.get(extensionName.toLowerCase()) == null,
                "Extension has already been unloaded: {0}", extensionName);

        LOGGER.info("Unloading extension {}", extensionName);
        extension.terminate();
        globalEventNode.removeChild(extension.eventNode());
        extensions.remove(extensionName.toLowerCase());
    }
}
