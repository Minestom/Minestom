package net.minestom.server.extensions;

import com.google.gson.Gson;
import net.minestom.dependencies.DependencyGetter;
import net.minestom.dependencies.ResolvedDependency;
import net.minestom.dependencies.maven.MavenRepository;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.selfmodification.MinestomExtensionClassLoader;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.throwables.MixinError;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.service.ServiceNotAvailableError;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class ExtensionManager {

    public final static String DISABLE_EARLY_LOAD_SYSTEM_KEY = "minestom.extension.disable_early_load";

    public final static Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    public final static String INDEV_CLASSES_FOLDER = "minestom.extension.indevfolder.classes";
    public final static String INDEV_RESOURCES_FOLDER = "minestom.extension.indevfolder.resources";
    private final static Gson GSON = new Gson();

    private final Map<String, MinestomExtensionClassLoader> extensionLoaders = new HashMap<>();
    private final Map<String, Extension> extensions = new HashMap<>();
    private final File extensionFolder = new File("extensions");
    private final File dependenciesFolder = new File(extensionFolder, ".libs");
    private boolean loaded;

    private final List<Extension> extensionList = new CopyOnWriteArrayList<>();
    private final List<Extension> immutableExtensionListView = Collections.unmodifiableList(extensionList);

    // Option
    private boolean loadOnStartup = true;

    public ExtensionManager() {
    }

    /**
     * Gets if the extensions should be loaded during startup.
     * <p>
     * Default value is 'true'.
     *
     * @return true if extensions are loaded in {@link net.minestom.server.MinecraftServer#start(String, int, ResponseDataConsumer)}
     */
    public boolean shouldLoadOnStartup() {
        return loadOnStartup;
    }

    /**
     * Used to specify if you want extensions to be loaded and initialized during startup.
     * <p>
     * Only useful before the server start.
     *
     * @param loadOnStartup true to load extensions on startup, false to do nothing
     */
    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public void loadExtensions() {
        Check.stateCondition(loaded, "Extensions are already loaded!");
        this.loaded = true;

        if (!extensionFolder.exists()) {
            if (!extensionFolder.mkdirs()) {
                LOGGER.error("Could not find or create the extension folder, extensions will not be loaded!");
                return;
            }
        }

        if (!dependenciesFolder.exists()) {
            if (!dependenciesFolder.mkdirs()) {
                LOGGER.error("Could not find nor create the extension dependencies folder, extensions will not be loaded!");
                return;
            }
        }

        List<DiscoveredExtension> discoveredExtensions = discoverExtensions();
        discoveredExtensions = generateLoadOrder(discoveredExtensions);
        loadDependencies(discoveredExtensions);
        // remove invalid extensions
        discoveredExtensions.removeIf(ext -> ext.loadStatus != DiscoveredExtension.LoadStatus.LOAD_SUCCESS);

        for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
            try {
                setupClassLoader(discoveredExtension);
            } catch (Exception e) {
                discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.FAILED_TO_SETUP_CLASSLOADER;
                MinecraftServer.getExceptionManager().handleException(e);
                LOGGER.error("Failed to load extension {}", discoveredExtension.getName());
                LOGGER.error("Failed to load extension", e);
            }
        }

        // remove invalid extensions
        discoveredExtensions.removeIf(ext -> ext.loadStatus != DiscoveredExtension.LoadStatus.LOAD_SUCCESS);
        setupCodeModifiers(discoveredExtensions);

        for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
            try {
                attemptSingleLoad(discoveredExtension);
            } catch (Exception e) {
                discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.LOAD_FAILED;
                LOGGER.error("Failed to load extension {}", discoveredExtension.getName());
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }

        // periodically cleanup observers
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for(Extension ext : extensionList) {
                ext.cleanupObservers();
            }
        }).repeat(1L, TimeUnit.MINUTE).schedule();
    }

    private void setupClassLoader(@NotNull DiscoveredExtension discoveredExtension) {
        final String extensionName = discoveredExtension.getName();

        final URL[] urls = discoveredExtension.files.toArray(new URL[0]);
        final MinestomExtensionClassLoader loader = newClassLoader(discoveredExtension, urls);

        extensionLoaders.put(extensionName.toLowerCase(), loader);
    }

    @Nullable
    private Extension attemptSingleLoad(@NotNull DiscoveredExtension discoveredExtension) {
        // Create ExtensionDescription (authors, version etc.)
        final String extensionName = discoveredExtension.getName();
        String mainClass = discoveredExtension.getEntrypoint();
        Extension.ExtensionDescription extensionDescription = new Extension.ExtensionDescription(
                extensionName,
                discoveredExtension.getVersion(),
                Arrays.asList(discoveredExtension.getAuthors()),
                discoveredExtension
        );

        MinestomExtensionClassLoader loader = extensionLoaders.get(extensionName.toLowerCase());

        if (extensions.containsKey(extensionName.toLowerCase())) {
            LOGGER.error("An extension called '{}' has already been registered.", extensionName);
            return null;
        }

        Class<?> jarClass;
        try {
            jarClass = Class.forName(mainClass, true, loader);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not find main class '{}' in extension '{}'.", mainClass, extensionName, e);
            return null;
        }

        Class<? extends Extension> extensionClass;
        try {
            extensionClass = jarClass.asSubclass(Extension.class);
        } catch (ClassCastException e) {
            LOGGER.error("Main class '{}' in '{}' does not extend the 'Extension' superclass.", mainClass, extensionName, e);
            return null;
        }

        Constructor<? extends Extension> constructor;
        try {
            constructor = extensionClass.getDeclaredConstructor();
            // Let's just make it accessible, plugin creators don't have to make this public.
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Main class '{}' in '{}' does not define a no-args constructor.", mainClass, extensionName, e);
            return null;
        }
        Extension extension = null;
        try {
            extension = constructor.newInstance();
        } catch (InstantiationException e) {
            LOGGER.error("Main class '{}' in '{}' cannot be an abstract class.", mainClass, extensionName, e);
            return null;
        } catch (IllegalAccessException ignored) {
            // We made it accessible, should not occur
        } catch (InvocationTargetException e) {
            LOGGER.error(
                    "While instantiating the main class '{}' in '{}' an exception was thrown.",
                    mainClass,
                    extensionName,
                    e.getTargetException()
            );
            return null;
        }

        // Set extension description
        try {
            Field descriptionField = Extension.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(extension, extensionDescription);
        } catch (IllegalAccessException e) {
            // We made it accessible, should not occur
        } catch (NoSuchFieldException e) {
            LOGGER.error("Main class '{}' in '{}' has no description field.", mainClass, extensionName, e);
            return null;
        }

        // Set logger
        try {
            Field loggerField = Extension.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(extension, LoggerFactory.getLogger(extensionClass));
        } catch (IllegalAccessException e) {
            // We made it accessible, should not occur
            MinecraftServer.getExceptionManager().handleException(e);
        } catch (NoSuchFieldException e) {
            // This should also not occur (unless someone changed the logger in Extension superclass).
            LOGGER.error("Main class '{}' in '{}' has no logger field.", mainClass, extensionName, e);
        }

        // add dependents to pre-existing extensions, so that they can easily be found during reloading
        for (String dependency : discoveredExtension.getDependencies()) {
            Extension dep = extensions.get(dependency.toLowerCase());
            if (dep == null) {
                LOGGER.warn("Dependency {} of {} is null? This means the extension has been loaded without its dependency, which could cause issues later.", dependency, discoveredExtension.getName());
            } else {
                dep.getDescription().getDependents().add(discoveredExtension.getName());
            }
        }

        extensionList.add(extension); // add to a list, as lists preserve order
        extensions.put(extensionName.toLowerCase(), extension);

        return extension;
    }

    @NotNull
    private List<DiscoveredExtension> discoverExtensions() {
        List<DiscoveredExtension> extensions = new LinkedList<>();
        File[] fileList = extensionFolder.listFiles();
        if(fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    continue;
                }
                if (!file.getName().endsWith(".jar")) {
                    continue;
                }
                DiscoveredExtension extension = discoverFromJar(file);
                if (extension != null && extension.loadStatus == DiscoveredExtension.LoadStatus.LOAD_SUCCESS) {
                    extensions.add(extension);
                }
            }
        }

        // this allows developers to have their extension discovered while working on it, without having to build a jar and put in the extension folder
        if (System.getProperty(INDEV_CLASSES_FOLDER) != null && System.getProperty(INDEV_RESOURCES_FOLDER) != null) {
            LOGGER.info("Found indev folders for extension. Adding to list of discovered extensions.");
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
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }
        return extensions;
    }

    private DiscoveredExtension discoverFromJar(File file) {
        try (ZipFile f = new ZipFile(file);
             InputStreamReader reader = new InputStreamReader(f.getInputStream(f.getEntry("extension.json")))) {

            DiscoveredExtension extension = GSON.fromJson(reader, DiscoveredExtension.class);
            extension.setOriginalJar(file);
            extension.files.add(file.toURI().toURL());

            // Verify integrity and ensure defaults
            DiscoveredExtension.verifyIntegrity(extension);

            return extension;
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    @Nullable
    private List<DiscoveredExtension> generateLoadOrder(@NotNull List<DiscoveredExtension> discoveredExtensions) {
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
                            // attempt to see if it is not already loaded (happens with dynamic (re)loading)
                            if (extensions.containsKey(dependencyName.toLowerCase())) {
                                return extensions.get(dependencyName.toLowerCase()).getDescription().getOrigin();
                            } else {
                                LOGGER.error("Extension {} requires an extension called {}.", discoveredExtension.getName(), dependencyName);
                                LOGGER.error("However the extension {} could not be found.", dependencyName);
                                LOGGER.error("Therefore {} will not be loaded.", discoveredExtension.getName());
                                discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.MISSING_DEPENDENCIES;
                            }
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
                loadableExtensions = dependencyMap.entrySet().stream().filter(entry -> areAllDependenciesLoaded(entry.getValue())).collect(Collectors.toList())
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
            LOGGER.error("Minestom found {} cyclic extensions.", dependencyMap.size());
            LOGGER.error("Cyclic extensions depend on each other and can therefore not be loaded.");
            for (Map.Entry<DiscoveredExtension, List<DiscoveredExtension>> entry : dependencyMap.entrySet()) {
                DiscoveredExtension discoveredExtension = entry.getKey();
                LOGGER.error("{} could not be loaded, as it depends on: {}.",
                        discoveredExtension.getName(),
                        entry.getValue().stream().map(DiscoveredExtension::getName).collect(Collectors.joining(", ")));
            }

        }

        return sortedList;
    }

    private boolean areAllDependenciesLoaded(@NotNull List<DiscoveredExtension> dependencies) {
        return dependencies.isEmpty() || dependencies.stream().allMatch(ext -> extensions.containsKey(ext.getName().toLowerCase()));
    }

    private void loadDependencies(List<DiscoveredExtension> extensions) {
        List<DiscoveredExtension> allLoadedExtensions = new LinkedList<>(extensions);
        extensionList.stream().map(ext -> ext.getDescription().getOrigin()).forEach(allLoadedExtensions::add);
        ExtensionDependencyResolver extensionDependencyResolver = new ExtensionDependencyResolver(allLoadedExtensions);
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
                    addDependencyFile(resolved, ext);
                    LOGGER.trace("Dependency of extension {}: {}", ext.getName(), resolved);
                }

                for (var dependencyName : ext.getDependencies()) {
                    var resolved = getter.get(dependencyName, dependenciesFolder);
                    addDependencyFile(resolved, ext);
                    LOGGER.trace("Dependency of extension {}: {}", ext.getName(), resolved);
                }
            } catch (Exception e) {
                ext.loadStatus = DiscoveredExtension.LoadStatus.MISSING_DEPENDENCIES;
                LOGGER.error("Failed to load dependencies for extension {}", ext.getName());
                LOGGER.error("Extension '{}' will not be loaded", ext.getName());
                LOGGER.error("This is the exception", e);
            }
        }
    }

    private void addDependencyFile(ResolvedDependency dependency, DiscoveredExtension extension) {
        URL location = dependency.getContentsLocation();
        extension.files.add(location);
        LOGGER.trace("Added dependency {} to extension {} classpath", location.toExternalForm(), extension.getName());

        // recurse to add full dependency tree
        if(!dependency.getSubdependencies().isEmpty()) {
            LOGGER.trace("Dependency {} has subdependencies, adding...", location.toExternalForm());
            for(ResolvedDependency sub : dependency.getSubdependencies()) {
                addDependencyFile(sub, extension);
            }
            LOGGER.trace("Dependency {} has had its subdependencies added.", location.toExternalForm());
        }
    }

    /**
     * Creates a new class loader for the given extension.
     * Will add the new loader as a child of all its dependencies' loaders.
     *
     * @param urls {@link URL} (usually a JAR) that should be loaded.
     */
    @NotNull
    public MinestomExtensionClassLoader newClassLoader(@NotNull DiscoveredExtension extension, @NotNull URL[] urls) {
        MinestomRootClassLoader root = MinestomRootClassLoader.getInstance();
        MinestomExtensionClassLoader loader = new MinestomExtensionClassLoader(extension.getName(), extension.getEntrypoint(), urls, root);
        if (extension.getDependencies().length == 0) {
            // orphaned extension, we can insert it directly
            root.addChild(loader);
        } else {
            // we need to keep track that it has actually been inserted
            // even though it should always be (due to the order in which extensions are loaders), it is an additional layer of """security"""
            boolean foundOne = false;
            for (String dependency : extension.getDependencies()) {
                if (extensionLoaders.containsKey(dependency.toLowerCase())) {
                    MinestomExtensionClassLoader parentLoader = extensionLoaders.get(dependency.toLowerCase());
                    parentLoader.addChild(loader);
                    foundOne = true;
                }
            }

            if (!foundOne) {
                LOGGER.error("Could not load extension {}, could not find any parent inside classloader hierarchy.", extension.getName());
                throw new RuntimeException("Could not load extension " + extension.getName() + ", could not find any parent inside classloader hierarchy.");
            }
        }
        return loader;
    }

    @NotNull
    public File getExtensionFolder() {
        return extensionFolder;
    }

    @NotNull
    public List<Extension> getExtensions() {
        return immutableExtensionListView;
    }

    @Nullable
    public Extension getExtension(@NotNull String name) {
        return extensions.get(name.toLowerCase());
    }

    @NotNull
    public Map<String, MinestomExtensionClassLoader> getExtensionLoaders() {
        return new HashMap<>(extensionLoaders);
    }

    /**
     * Extensions are allowed to apply Mixin transformers, the magic happens here.
     */
    private void setupCodeModifiers(@NotNull List<DiscoveredExtension> extensions) {
        final ClassLoader cl = getClass().getClassLoader();
        if (!(cl instanceof MinestomRootClassLoader)) {
            LOGGER.warn("Current class loader is not a MinestomOverwriteClassLoader, but {}. " +
                    "This disables code modifiers (Mixin support is therefore disabled). " +
                    "This can be fixed by starting your server using Bootstrap#bootstrap (optional).", cl);
            return;
        }
        MinestomRootClassLoader modifiableClassLoader = (MinestomRootClassLoader) cl;
        setupCodeModifiers(extensions, modifiableClassLoader);
    }

    private void setupCodeModifiers(@NotNull List<DiscoveredExtension> extensions, MinestomRootClassLoader modifiableClassLoader) {
        LOGGER.info("Start loading code modifiers...");
        for (DiscoveredExtension extension : extensions) {
            try {
                for (String codeModifierClass : extension.getCodeModifiers()) {
                    boolean loaded = modifiableClassLoader.loadModifier(extension.files.toArray(new URL[0]), codeModifierClass);
                    if(!loaded) {
                        extension.addMissingCodeModifier(codeModifierClass);
                    }
                }
                if (!extension.getMixinConfig().isEmpty()) {
                    final String mixinConfigFile = extension.getMixinConfig();
                    try {
                        Mixins.addConfiguration(mixinConfigFile);
                        LOGGER.info("Found mixin in extension {}: {}", extension.getName(), mixinConfigFile);
                    } catch (ServiceNotAvailableError | MixinError | MixinException e) {
                        if(MinecraftServer.getExceptionManager() != null) {
                            MinecraftServer.getExceptionManager().handleException(e);
                        } else {
                            e.printStackTrace();
                        }
                        LOGGER.error("Could not load Mixin configuration: "+mixinConfigFile);
                        extension.setFailedToLoadMixinFlag();
                    }
                }
            } catch (Exception e) {
                if(MinecraftServer.getExceptionManager() != null) {
                    MinecraftServer.getExceptionManager().handleException(e);
                } else {
                    e.printStackTrace();
                }
                LOGGER.error("Failed to load code modifier for extension in files: " +
                        extension.files
                                .stream()
                                .map(URL::toExternalForm)
                                .collect(Collectors.joining(", ")), e);
            }
        }
        LOGGER.info("Done loading code modifiers.");
    }

    private void unload(Extension ext) {
        ext.preTerminate();
        ext.terminate();
        // remove callbacks for this extension
        String extensionName = ext.getDescription().getName();
        ext.triggerChange(observer -> observer.onExtensionUnload(extensionName));
        // TODO: more callback types

        ext.postTerminate();
        ext.unload();

        // remove as dependent of other extensions
        // this avoids issues where a dependent extension fails to reload, and prevents the base extension to reload too
        for (Extension e : extensionList) {
            e.getDescription().getDependents().remove(ext.getDescription().getName());
        }

        String id = ext.getDescription().getName().toLowerCase();
        // remove from loaded extensions
        extensions.remove(id);
        extensionList.remove(ext);

        // remove class loader, required to reload the classes
        MinestomExtensionClassLoader classloader = extensionLoaders.remove(id);
        try {
            // close resources
            classloader.close();
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        MinestomRootClassLoader.getInstance().removeChildInHierarchy(classloader);
    }

    public void reload(String extensionName) {
        Extension ext = extensions.get(extensionName.toLowerCase());
        if (ext == null) {
            throw new IllegalArgumentException("Extension " + extensionName + " is not currently loaded.");
        }

        File originalJar = ext.getDescription().getOrigin().getOriginalJar();
        if (originalJar == null) {
            LOGGER.error("Cannot reload extension {} that is not from a .jar file!", extensionName);
            return;
        }

        LOGGER.info("Reload extension {} from jar file {}", extensionName, originalJar.getAbsolutePath());
        List<String> dependents = new LinkedList<>(ext.getDescription().getDependents()); // copy dependents list
        List<File> originalJarsOfDependents = new LinkedList<>();

        for (String dependentID : dependents) {
            Extension dependentExt = extensions.get(dependentID.toLowerCase());
            File dependentOriginalJar = dependentExt.getDescription().getOrigin().getOriginalJar();
            originalJarsOfDependents.add(dependentOriginalJar);
            if (dependentOriginalJar == null) {
                LOGGER.error("Cannot reload extension {} that is not from a .jar file!", dependentID);
                return;
            }

            LOGGER.info("Unloading dependent extension {} (because it depends on {})", dependentID, extensionName);
            unload(dependentExt);
        }

        LOGGER.info("Unloading extension {}", extensionName);
        unload(ext);

        System.gc();

        // ext and its dependents should no longer be referenced from now on

        // rediscover extension to reload. We allow dependency changes, so we need to fully reload it
        List<DiscoveredExtension> extensionsToReload = new LinkedList<>();
        LOGGER.info("Rediscover extension {} from jar {}", extensionName, originalJar.getAbsolutePath());
        DiscoveredExtension rediscoveredExtension = discoverFromJar(originalJar);
        extensionsToReload.add(rediscoveredExtension);

        for (File dependentJar : originalJarsOfDependents) {
            // rediscover dependent extension to reload
            LOGGER.info("Rediscover dependent extension (depends on {}) from jar {}", extensionName, dependentJar.getAbsolutePath());
            extensionsToReload.add(discoverFromJar(dependentJar));
        }

        // ensure correct order of dependencies
        loadExtensionList(extensionsToReload);
    }

    public boolean loadDynamicExtension(File jarFile) throws FileNotFoundException {
        if (!jarFile.exists()) {
            throw new FileNotFoundException("File '" + jarFile.getAbsolutePath() + "' does not exists. Cannot load extension.");
        }

        LOGGER.info("Discover dynamic extension from jar {}", jarFile.getAbsolutePath());
        DiscoveredExtension discoveredExtension = discoverFromJar(jarFile);
        List<DiscoveredExtension> extensionsToLoad = Collections.singletonList(discoveredExtension);
        return loadExtensionList(extensionsToLoad);
    }

    private boolean loadExtensionList(List<DiscoveredExtension> extensionsToLoad) {
        // ensure correct order of dependencies
        LOGGER.debug("Reorder extensions to ensure proper load order");
        extensionsToLoad = generateLoadOrder(extensionsToLoad);
        loadDependencies(extensionsToLoad);

        // setup new classloaders for the extensions to reload
        for (DiscoveredExtension toReload : extensionsToLoad) {
            LOGGER.debug("Setting up classloader for extension {}", toReload.getName());
            setupClassLoader(toReload);
        }

        // setup code modifiers for these extensions
        // TODO: it is possible the new modifiers cannot be applied (because the targeted classes are already loaded), should we issue a warning?
        setupCodeModifiers(extensionsToLoad);

        List<Extension> newExtensions = new LinkedList<>();
        for (DiscoveredExtension toReload : extensionsToLoad) {
            // reload extensions
            LOGGER.info("Actually load extension {}", toReload.getName());
            Extension loadedExtension = attemptSingleLoad(toReload);
            if (loadedExtension != null) {
                newExtensions.add(loadedExtension);
            }
        }

        if (newExtensions.isEmpty()) {
            LOGGER.error("No extensions to load, skipping callbacks");
            return false;
        }

        LOGGER.info("Load complete, firing preinit, init and then postinit callbacks");
        // retrigger preinit, init and postinit
        newExtensions.forEach(Extension::preInitialize);
        newExtensions.forEach(Extension::initialize);
        newExtensions.forEach(Extension::postInitialize);
        return true;
    }

    public void unloadExtension(String extensionName) {
        Extension ext = extensions.get(extensionName.toLowerCase());
        if (ext == null) {
            throw new IllegalArgumentException("Extension " + extensionName + " is not currently loaded.");
        }
        List<String> dependents = new LinkedList<>(ext.getDescription().getDependents()); // copy dependents list

        for (String dependentID : dependents) {
            Extension dependentExt = extensions.get(dependentID.toLowerCase());
            LOGGER.info("Unloading dependent extension {} (because it depends on {})", dependentID, extensionName);
            unload(dependentExt);
        }

        LOGGER.info("Unloading extension {}", extensionName);
        unload(ext);

        // call GC to try to get rid of classes and classloader
        System.gc();
    }

    /**
     * Shutdowns all the extensions by unloading them.
     */
    public void shutdown() {
        this.extensionList.forEach(this::unload);
    }

    /**
     * Loads code modifiers early, that is before <code>MinecraftServer.init()</code> is called.
     */
    public static void loadCodeModifiersEarly() {
        // allow users to disable early code modifier load
        if("true".equalsIgnoreCase(System.getProperty(DISABLE_EARLY_LOAD_SYSTEM_KEY))) {
            return;
        }
        LOGGER.info("Early load of code modifiers from extensions.");
        ExtensionManager manager = new ExtensionManager();

        // discover extensions that are present
        List<DiscoveredExtension> discovered = manager.discoverExtensions();

        // setup extension class loaders, so that Mixin can load the json configuration file correctly
        for(DiscoveredExtension e : discovered) {
            manager.setupClassLoader(e);
        }

        // setup code modifiers and mixins
        manager.setupCodeModifiers(discovered, MinestomRootClassLoader.getInstance());

        // setup is done, remove all extension classloaders
        for(MinestomExtensionClassLoader extensionLoader : manager.getExtensionLoaders().values()) {
            MinestomRootClassLoader.getInstance().removeChildInHierarchy(extensionLoader);
        }
        LOGGER.info("Early load of code modifiers from extensions done!");
    }

    /**
     * Unloads all extensions
     */
    public void unloadAllExtensions() {
        // copy names, as the extensions map will be modified via the calls to unload
        Set<String> extensionNames = new HashSet<>(extensions.keySet());
        for(String ext : extensionNames) {
            if(extensions.containsKey(ext)) { // is still loaded? Because extensions can depend on one another, it might have already been unloaded
                unloadExtension(ext);
            }
        }
    }
}
