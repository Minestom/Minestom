package net.minestom.server.extensions;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.minestom.dependencies.DependencyGetter;
import net.minestom.dependencies.maven.MavenRepository;
import net.minestom.server.extras.selfmodification.MinestomExtensionClassLoader;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

@Slf4j(topic = "Minestom-Extensions")
public class ExtensionManager {

    private final static String INDEV_CLASSES_FOLDER = "minestom.extension.indevfolder.classes";
    private final static String INDEV_RESOURCES_FOLDER = "minestom.extension.indevfolder.resources";
    private final static Gson GSON = new Gson();

    private final Map<String, URLClassLoader> extensionLoaders = new HashMap<>();
    private final Map<String, Extension> extensions = new HashMap<>();
    private final File extensionFolder = new File("extensions");
    private final File dependenciesFolder = new File(extensionFolder, ".libs");
    private boolean loaded;

    // not final to add to it, and then make it immutable
    private List<Extension> extensionList = new ArrayList<>();

    public ExtensionManager() {
    }

    public void loadExtensions() {
        Check.stateCondition(loaded, "Extensions are already loaded!");
        this.loaded = true;

        if (!extensionFolder.exists()) {
            if (!extensionFolder.mkdirs()) {
                log.error("Could not find or create the extension folder, extensions will not be loaded!");
                return;
            }
        }

        if (!dependenciesFolder.exists()) {
            if (!dependenciesFolder.mkdirs()) {
                log.error("Could not find nor create the extension dependencies folder, extensions will not be loaded!");
                return;
            }
        }

        List<DiscoveredExtension> discoveredExtensions = discoverExtensions();
        discoveredExtensions = generateLoadOrder(discoveredExtensions);
        loadDependencies(discoveredExtensions);
        // remove invalid extensions
        discoveredExtensions.removeIf(ext -> ext.loadStatus != DiscoveredExtension.LoadStatus.LOAD_SUCCESS);
        setupCodeModifiers(discoveredExtensions);

        for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
            URLClassLoader loader;
            URL[] urls = discoveredExtension.files.toArray(new URL[0]);
            // TODO: Only putting each extension into its own classloader prevents code modifications (via code modifiers or mixins)
            // TODO: If we want modifications to be possible, we need to add these urls to the current classloader
            // TODO: Indeed, without adding the urls, the classloader is not able to load the bytecode of extension classes
            // TODO: Whether we want to allow extensions to modify one-another is our choice now.
            loader = newClassLoader(discoveredExtension, urls);

            // Create ExtensionDescription (authors, version etc.)
            String extensionName = discoveredExtension.getName();
            String mainClass = discoveredExtension.getEntrypoint();
            Extension.ExtensionDescription extensionDescription = new Extension.ExtensionDescription(
                    extensionName,
                    discoveredExtension.getVersion(),
                    Arrays.asList(discoveredExtension.getAuthors())
            );

            extensionLoaders.put(extensionName.toLowerCase(), loader);

            if (extensions.containsKey(extensionName.toLowerCase())) {
                log.error("An extension called '{}' has already been registered.", extensionName);
                continue;
            }

            Class<?> jarClass;
            try {
                jarClass = Class.forName(mainClass, true, loader);
            } catch (ClassNotFoundException e) {
                log.error("Could not find main class '{}' in extension '{}'.", mainClass, extensionName, e);
                continue;
            }

            Class<? extends Extension> extensionClass;
            try {
                extensionClass = jarClass.asSubclass(Extension.class);
            } catch (ClassCastException e) {
                log.error("Main class '{}' in '{}' does not extend the 'Extension' superclass.", mainClass, extensionName, e);
                continue;
            }

            Constructor<? extends Extension> constructor;
            try {
                constructor = extensionClass.getDeclaredConstructor();
                // Let's just make it accessible, plugin creators don't have to make this public.
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                log.error("Main class '{}' in '{}' does not define a no-args constructor.", mainClass, extensionName, e);
                continue;
            }
            Extension extension = null;
            try {
                extension = constructor.newInstance();
            } catch (InstantiationException e) {
                log.error("Main class '{}' in '{}' cannot be an abstract class.", mainClass, extensionName, e);
                continue;
            } catch (IllegalAccessException ignored) {
                // We made it accessible, should not occur
            } catch (InvocationTargetException e) {
                log.error(
                        "While instantiating the main class '{}' in '{}' an exception was thrown.",
                        mainClass,
                        extensionName,
                        e.getTargetException()
                );
                continue;
            }

            // Set extension description
            try {
                Field descriptionField = extensionClass.getSuperclass().getDeclaredField("description");
                descriptionField.setAccessible(true);
                descriptionField.set(extension, extensionDescription);
            } catch (IllegalAccessException e) {
                // We made it accessible, should not occur
            } catch (NoSuchFieldException e) {
                log.error("Main class '{}' in '{}' has no description field.", mainClass, extensionName, e);
                continue;
            }

            // Set logger
            try {
                Field loggerField = extensionClass.getSuperclass().getDeclaredField("logger");
                loggerField.setAccessible(true);
                loggerField.set(extension, LoggerFactory.getLogger(extensionClass));
            } catch (IllegalAccessException e) {
                // We made it accessible, should not occur
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                // This should also not occur (unless someone changed the logger in Extension superclass).
                log.error("Main class '{}' in '{}' has no logger field.", mainClass, extensionName, e);
            }

            extensionList.add(extension); // add to a list, as lists preserve order
            extensions.put(extensionName.toLowerCase(), extension);
        }
        extensionList = Collections.unmodifiableList(extensionList);
    }

    @NotNull
    private List<DiscoveredExtension> discoverExtensions() {
        List<DiscoveredExtension> extensions = new LinkedList<>();
        for (File file : extensionFolder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            try (ZipFile f = new ZipFile(file);
                 InputStreamReader reader = new InputStreamReader(f.getInputStream(f.getEntry("extension.json")))) {

                DiscoveredExtension extension = GSON.fromJson(reader, DiscoveredExtension.class);
                extension.files.add(file.toURI().toURL());

                // Verify integrity and ensure defaults
                DiscoveredExtension.verifyIntegrity(extension);

                if (extension.loadStatus == DiscoveredExtension.LoadStatus.LOAD_SUCCESS) {
                    extensions.add(extension);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // this allows developers to have their extension discovered while working on it, without having to build a jar and put in the extension folder
        if (System.getProperty(INDEV_CLASSES_FOLDER) != null && System.getProperty(INDEV_RESOURCES_FOLDER) != null) {
            log.info("Found indev folders for extension. Adding to list of discovered extensions.");
            final String extensionClasses = System.getProperty(INDEV_CLASSES_FOLDER);
            final String extensionResources = System.getProperty(INDEV_RESOURCES_FOLDER);
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(extensionResources, "extension.json")))) {
                DiscoveredExtension extension = GSON.fromJson(reader, DiscoveredExtension.class);
                extension.files.add(new File(extensionClasses).toURI().toURL());
                extension.files.add(new File(extensionResources).toURI().toURL());

                // Verify integrity and ensure defaults
                DiscoveredExtension.verifyIntegrity(extension);

                if (extension.loadStatus == DiscoveredExtension.LoadStatus.LOAD_SUCCESS) {
                    extensions.add(extension);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return extensions;
    }

    private List<DiscoveredExtension> generateLoadOrder(List<DiscoveredExtension> discoveredExtensions) {
        // Do some mapping so we can map strings to extensions.
        Map<String, DiscoveredExtension> extensionMap = new HashMap<>();
        Map<DiscoveredExtension, List<DiscoveredExtension>> dependencyMap = new HashMap<>();
        for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
            extensionMap.put(discoveredExtension.getName().toLowerCase(), discoveredExtension);
        }
        for (DiscoveredExtension discoveredExtension : discoveredExtensions) {

            List<DiscoveredExtension> dependencies = Arrays.stream(discoveredExtension.getDependencies())
                    .map(dependencyName -> {
                        DiscoveredExtension dependencyExtension = extensionMap.get(dependencyName.toLowerCase());
                        // Specifies an extension we don't have.
                        if (dependencyExtension == null) {
                            log.error("Extension {} requires an extension called {}.", discoveredExtension.getName(), dependencyName);
                            log.error("However the extension {} could not be found.", dependencyName);
                            log.error("Therefore {} will not be loaded.", dependencyName);
                            discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.MISSING_DEPENDENCIES;
                        }
                        // This will return null for an unknown-extension
                        return extensionMap.get(dependencyName.toLowerCase());
                    }).collect(Collectors.toList());

            // If the list contains null ignore it.
            if (!dependencies.contains(null)) {
                dependencyMap.put(
                        discoveredExtension,
                        dependencies
                );
            }
        }

        // List containing the real load order.
        LinkedList<DiscoveredExtension> sortedList = new LinkedList<>();

        // entries with empty lists
        List<Map.Entry<DiscoveredExtension, List<DiscoveredExtension>>> loadableExtensions;
        // While there are entries with no more elements (no more dependencies)
        while (!(
                loadableExtensions = dependencyMap.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).collect(Collectors.toList())
        ).isEmpty()
        ) {
            // Get all "loadable" (not actually being loaded!) extensions and put them in the sorted list.
            for (Map.Entry<DiscoveredExtension, List<DiscoveredExtension>> entry : loadableExtensions) {
                // Add to sorted list.
                sortedList.add(entry.getKey());
                // Remove to make the next iterations a little bit quicker (hopefully) and to find cyclic dependencies.
                dependencyMap.remove(entry.getKey());
                // Remove this dependency from all the lists (if they include it) to make way for next level of extensions.
                dependencyMap.forEach((key, dependencyList) -> dependencyList.remove(entry.getKey()));
            }
        }

        // Check if there are cyclic extensions.
        if (!dependencyMap.isEmpty()) {
            log.error("Minestom found " + dependencyMap.size() + " cyclic extensions.");
            log.error("Cyclic extensions depend on each other and can therefore not be loaded.");
            for (Map.Entry<DiscoveredExtension, List<DiscoveredExtension>> entry : dependencyMap.entrySet()) {
                DiscoveredExtension discoveredExtension = entry.getKey();
                log.error(discoveredExtension.getName() + " could not be loaded, as it depends on: "
                        + entry.getValue().stream().map(DiscoveredExtension::getName).collect(Collectors.joining(", "))
                        + "."
                );
            }

        }

        return sortedList;
    }

    private void loadDependencies(List<DiscoveredExtension> extensions) {
        ExtensionDependencyResolver extensionDependencyResolver = new ExtensionDependencyResolver(extensions);
        for (DiscoveredExtension ext : extensions) {
            try {
                DependencyGetter getter = new DependencyGetter();
                DiscoveredExtension.ExternalDependencies externalDependencies = ext.getExternalDependencies();
                List<MavenRepository> repoList = new LinkedList<>();
                for (var repository : externalDependencies.repositories) {
                    if (repository.name == null) {
                        throw new IllegalStateException("Missing 'name' element in repository object.");
                    }
                    if (repository.name.isEmpty()) {
                        throw new IllegalStateException("Invalid 'name' element in repository object.");
                    }
                    if (repository.url == null) {
                        throw new IllegalStateException("Missing 'url' element in repository object.");
                    }
                    if (repository.url.isEmpty()) {
                        throw new IllegalStateException("Invalid 'url' element in repository object.");
                    }
                    repoList.add(new MavenRepository(repository.name, repository.url));
                }
                getter.addMavenResolver(repoList);
                getter.addResolver(extensionDependencyResolver);

                for (var artifact : externalDependencies.artifacts) {
                    var resolved = getter.get(artifact, dependenciesFolder);
                    addDependencyFile(resolved.getContentsLocation(), ext);
                    log.trace("Dependency of extension {}: {}", ext.getName(), resolved);
                }

                for (var dependencyName : ext.getDependencies()) {
                    var resolved = getter.get(dependencyName, dependenciesFolder);
                    addDependencyFile(resolved.getContentsLocation(), ext);
                    log.trace("Dependency of extension {}: {}", ext.getName(), resolved);
                }
            } catch (Exception e) {
                ext.loadStatus = DiscoveredExtension.LoadStatus.MISSING_DEPENDENCIES;
                log.error("Failed to load dependencies for extension {}", ext.getName());
                log.error("Extension '{}' will not be loaded", ext.getName());
                log.error("This is the exception", e);
            }
        }
    }

    // TODO: remove if extensions cannot modify one-another
    // TODO: use if they can
    private void injectIntoClasspath(URL dependency, DiscoveredExtension extension) {
        final ClassLoader cl = getClass().getClassLoader();
        if (!(cl instanceof URLClassLoader)) {
            throw new IllegalStateException("Current class loader is not a URLClassLoader, but " + cl + ". This prevents adding URLs into the classpath at runtime.");
        }
        if(cl instanceof MinestomRootClassLoader) {
            ((MinestomRootClassLoader) cl).addURL(dependency); // no reflection warnings for us!
        } else {
            try {
                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                addURL.invoke(cl, dependency);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Failed to inject URL " + dependency + " into classpath. From extension " + extension.getName(), e);
            }
        }
    }

    private void addDependencyFile(URL dependency, DiscoveredExtension extension) {
        extension.files.add(dependency);
        log.trace("Added dependency {} to extension {} classpath", dependency.toExternalForm(), extension.getName());
    }

    /**
     * Loads a URL into the classpath.
     *
     * @param urls {@link URL} (usually a JAR) that should be loaded.
     */
    @NotNull
    public URLClassLoader newClassLoader(@NotNull DiscoveredExtension extension, @NotNull URL[] urls) {
        MinestomRootClassLoader root = MinestomRootClassLoader.getInstance();
        MinestomExtensionClassLoader loader = new MinestomExtensionClassLoader(extension.getName(), urls, root);
        // TODO: tree structure
        root.addChild(loader);
        return loader;
    }

    @NotNull
    public File getExtensionFolder() {
        return extensionFolder;
    }

    @NotNull
    public List<Extension> getExtensions() {
        return extensionList;
    }

    @Nullable
    public Extension getExtension(@NotNull String name) {
        return extensions.get(name.toLowerCase());
    }

    @NotNull
    public Map<String, URLClassLoader> getExtensionLoaders() {
        return new HashMap<>(extensionLoaders);
    }

    /**
     * Extensions are allowed to apply Mixin transformers, the magic happens here.
     */
    private void setupCodeModifiers(@NotNull List<DiscoveredExtension> extensions) {
        final ClassLoader cl = getClass().getClassLoader();
        if (!(cl instanceof MinestomRootClassLoader)) {
            log.warn("Current class loader is not a MinestomOverwriteClassLoader, but " + cl + ". This disables code modifiers (Mixin support is therefore disabled)");
            return;
        }
        MinestomRootClassLoader modifiableClassLoader = (MinestomRootClassLoader) cl;
        log.info("Start loading code modifiers...");
        for (DiscoveredExtension extension : extensions) {
            try {
                for (String codeModifierClass : extension.getCodeModifiers()) {
                    modifiableClassLoader.loadModifier(extension.files.toArray(new File[0]), codeModifierClass);
                }
                if (!extension.getMixinConfig().isEmpty()) {
                    final String mixinConfigFile = extension.getMixinConfig();
                    Mixins.addConfiguration(mixinConfigFile);
                    log.info("Found mixin in extension " + extension.getName() + ": " + mixinConfigFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Failed to load code modifier for extension in files: " + extension.files.stream().map(u -> u.toExternalForm()).collect(Collectors.joining(", ")), e);
            }
        }
        log.info("Done loading code modifiers.");
    }
}
