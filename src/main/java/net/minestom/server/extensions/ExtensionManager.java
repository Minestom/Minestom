package net.minestom.server.extensions;

import com.google.gson.Gson;
import net.minestom.dependencies.DependencyGetter;
import net.minestom.dependencies.ResolvedDependency;
import net.minestom.dependencies.maven.MavenRepository;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
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
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtensionManager {

    public final static String DISABLE_EARLY_LOAD_SYSTEM_KEY = "minestom.extension.disable_early_load";

    public final static Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    public final static String INDEV_CLASSES_FOLDER = "minestom.extension.indevfolder.classes";
    public final static String INDEV_RESOURCES_FOLDER = "minestom.extension.indevfolder.resources";
    private final static Gson GSON = new Gson();

    // LinkedHashMaps are HashMaps that preserve order
    private final Map<String, Extension> extensions = new LinkedHashMap<>();
    private final Map<String, Extension> immutableExtensions = Collections.unmodifiableMap(extensions);

    private final File extensionFolder = new File("extensions");
    private final File dependenciesFolder = new File(extensionFolder, ".libs");
    private Path extensionDataRoot = extensionFolder.toPath();
    private boolean loaded;

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

    /**
     * Loads all extensions in the extension folder into this server.
     * <br><br>
     * <p>
     * Pipeline:
     * <br>
     * Finds all .jar files in the extensions folder.
     * <br>
     * Per each jar:
     * <br>
     * Turns its extension.json into a DiscoveredExtension object.
     * <br>
     * Verifies that all properties of extension.json are correctly set.
     * <br><br>
     * <p>
     * It then sorts all those jars by their load order (making sure that an extension's dependencies load before it)
     * <br>
     * Note: Cyclic dependencies will stop both extensions from being loaded.
     * <br><br>
     * <p>
     * Afterwards, it loads all external dependencies and adds them to the extension's files
     * <br><br>
     * <p>
     * Then removes any invalid extensions (Invalid being its Load Status isn't SUCCESS)
     * <br><br>
     * <p>
     * After that, it set its classloaders so each extension is self-contained,
     * <br><br>
     * <p>
     * Removes invalid extensions again,
     * <br><br>
     * <p>
     * and loads all of those extensions into Minestom
     * <br>
     * (Extension fields are set via reflection after each extension is verified, then loaded.)
     * <br><br>
     * <p>
     * If the extension successfully loads, add it to the global extension Map (Name to Extension)
     * <br><br>
     * <p>
     * And finally make a scheduler to clean observers per extension.
     */
    public void loadExtensions() {
        Check.stateCondition(loaded, "Extensions are already loaded!");
        this.loaded = true;

        // Initialize folders
        {
            // Make extensions folder if necessary
            if (!extensionFolder.exists()) {
                if (!extensionFolder.mkdirs()) {
                    LOGGER.error("Could not find or create the extension folder, extensions will not be loaded!");
                    return;
                }
            }

            // Make dependencies folder if necessary
            if (!dependenciesFolder.exists()) {
                if (!dependenciesFolder.mkdirs()) {
                    LOGGER.error("Could not find nor create the extension dependencies folder, extensions will not be loaded!");
                    return;
                }
            }
        }

        // Periodically cleanup observers
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (Extension ext : extensions.values()) {
                ext.cleanupObservers();
            }
        }).repeat(1L, TimeUnit.MINUTE).schedule();

        // Load extensions
        {
            // Get all extensions and order them accordingly.
            List<DiscoveredExtension> discoveredExtensions = discoverExtensions();

            // Don't waste resources on doing extra actions if there is nothing to do.
            if (discoveredExtensions.isEmpty()) return;

            discoveredExtensions = generateLoadOrder(discoveredExtensions);
            loadDependencies(discoveredExtensions);

            // remove invalid extensions
            discoveredExtensions.removeIf(ext -> ext.loadStatus != DiscoveredExtension.LoadStatus.LOAD_SUCCESS);

            // set class loaders for all extensions.
            for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
                try {
                    discoveredExtension.setMinestomExtensionClassLoader(discoveredExtension.makeClassLoader());
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

            // Load the extensions
            for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
                try {
                    loadExtension(discoveredExtension);
                } catch (Exception e) {
                    discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.LOAD_FAILED;
                    LOGGER.error("Failed to load extension {}", discoveredExtension.getName());
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
        }
    }

    /**
     * Loads an extension into Minestom.
     *
     * @param discoveredExtension The extension. Make sure to verify its integrity, set its class loader, and its files.
     * @return An extension object made from this DiscoveredExtension
     */
    @Nullable
    private Extension loadExtension(@NotNull DiscoveredExtension discoveredExtension) {
        // Create Extension (authors, version etc.)
        String extensionName = discoveredExtension.getName();
        String mainClass = discoveredExtension.getEntrypoint();

        MinestomExtensionClassLoader loader = discoveredExtension.getMinestomExtensionClassLoader();

        if (extensions.containsKey(extensionName.toLowerCase())) {
            LOGGER.error("An extension called '{}' has already been registered.", extensionName);
            return null;
        }

        Class<?> jarClass;
        try {
            jarClass = Class.forName(mainClass, true, loader);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not find main class '{}' in extension '{}'. If it is, be sure to run your server using Bootstrap#bootstrap",
                    mainClass, extensionName, e);
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

        // Set extension origin to its DiscoveredExtension
        try {
            Field originField = Extension.class.getDeclaredField("origin");
            originField.setAccessible(true);
            originField.set(extension, discoveredExtension);
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

        // Set event node
        try {
            EventNode<Event> eventNode = EventNode.all(extensionName); // Use the extension name
            Field loggerField = Extension.class.getDeclaredField("eventNode");
            loggerField.setAccessible(true);
            loggerField.set(extension, eventNode);

            MinecraftServer.getGlobalEventHandler().addChild(eventNode);
        } catch (IllegalAccessException e) {
            // We made it accessible, should not occur
            MinecraftServer.getExceptionManager().handleException(e);
        } catch (NoSuchFieldException e) {
            // This should also not occur
            LOGGER.error("Main class '{}' in '{}' has no event node field.", mainClass, extensionName, e);
        }

        // add dependents to pre-existing extensions, so that they can easily be found during reloading
        for (String dependencyName : discoveredExtension.getDependencies()) {
            Extension dependency = extensions.get(dependencyName.toLowerCase());
            if (dependency == null) {
                LOGGER.warn("Dependency {} of {} is null? This means the extension has been loaded without its dependency, which could cause issues later.", dependencyName, discoveredExtension.getName());
            } else {
                dependency.getDependents().add(discoveredExtension.getName());
            }
        }

        // add to a linked hash map, as they preserve order
        extensions.put(extensionName.toLowerCase(), extension);

        return extension;
    }

    /**
     * Get all extensions from the extensions folder and make them discovered.
     * <p>
     * It skims the extension folder, discovers and verifies each extension, and returns those created DiscoveredExtensions.
     *
     * @return A list of discovered extensions from this folder.
     */
    private @NotNull List<DiscoveredExtension> discoverExtensions() {
        List<DiscoveredExtension> extensions = new LinkedList<>();

        File[] fileList = extensionFolder.listFiles();

        if (fileList != null) {
            // Loop through all files in extension folder
            for (File file : fileList) {

                // Ignore folders
                if (file.isDirectory()) {
                    continue;
                }

                // Ignore non .jar files
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
                extension.setDataDirectory(getExtensionDataRoot().resolve(extension.getName()));

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

    /**
     * Grabs a discovered extension from a jar.
     *
     * @param file The jar to grab it from (a .jar is a formatted .zip file)
     * @return The created DiscoveredExtension.
     */
    private @Nullable DiscoveredExtension discoverFromJar(@NotNull File file) {
        try (ZipFile f = new ZipFile(file)) {

            ZipEntry entry = f.getEntry("extension.json");

            if (entry == null)
                throw new IllegalStateException("Missing extension.json in extension " + file.getName() + ".");

            InputStreamReader reader = new InputStreamReader(f.getInputStream(entry));

            // Initialize DiscoveredExtension from GSON.
            DiscoveredExtension extension = GSON.fromJson(reader, DiscoveredExtension.class);
            extension.setOriginalJar(file);
            extension.files.add(file.toURI().toURL());
            extension.setDataDirectory(getExtensionDataRoot().resolve(extension.getName()));

            // Verify integrity and ensure defaults
            DiscoveredExtension.verifyIntegrity(extension);

            return extension;
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    @NotNull
    private List<DiscoveredExtension> generateLoadOrder(@NotNull List<DiscoveredExtension> discoveredExtensions) {
        // Extension --> Extensions it depends on.
        Map<DiscoveredExtension, List<DiscoveredExtension>> dependencyMap = new HashMap<>();

        // Put dependencies in dependency map
        {
            Map<String, DiscoveredExtension> extensionMap = new HashMap<>();

            // go through all the discovered extensions and assign their name in a map.
            for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
                extensionMap.put(discoveredExtension.getName().toLowerCase(), discoveredExtension);
            }

            allExtensions:
            // go through all the discovered extensions and get their dependencies as extensions
            for (DiscoveredExtension discoveredExtension : discoveredExtensions) {

                List<DiscoveredExtension> dependencies = new ArrayList<>(discoveredExtension.getDependencies().length);

                // Map the dependencies into DiscoveredExtensions.
                for (String dependencyName : discoveredExtension.getDependencies()) {

                    DiscoveredExtension dependencyExtension = extensionMap.get(dependencyName.toLowerCase());
                    // Specifies an extension we don't have.
                    if (dependencyExtension == null) {

                        // attempt to see if it is not already loaded (happens with dynamic (re)loading)
                        if (extensions.containsKey(dependencyName.toLowerCase())) {

                            dependencies.add(extensions.get(dependencyName.toLowerCase()).getOrigin());
                            continue; // Go to the next loop in this dependency loop, this iteration is done.

                        } else {

                            // dependency isn't loaded, move on.
                            LOGGER.error("Extension {} requires an extension called {}.", discoveredExtension.getName(), dependencyName);
                            LOGGER.error("However the extension {} could not be found.", dependencyName);
                            LOGGER.error("Therefore {} will not be loaded.", discoveredExtension.getName());
                            discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.MISSING_DEPENDENCIES;
                            continue allExtensions; // the above labeled loop will go to the next extension as this dependency is invalid.

                        }
                    }
                    // This will add null for an unknown-extension
                    dependencies.add(dependencyExtension);

                }

                dependencyMap.put(
                        discoveredExtension,
                        dependencies
                );

            }
        }

        // List containing the load order.
        LinkedList<DiscoveredExtension> sortedList = new LinkedList<>();

        // TODO actually have to read this
        {
            // entries with empty lists
            List<Map.Entry<DiscoveredExtension, List<DiscoveredExtension>>> loadableExtensions;

            // While there are entries with no more elements (no more dependencies)
            while (!(
                    loadableExtensions = dependencyMap.entrySet().stream().filter(entry -> isLoaded(entry.getValue())).collect(Collectors.toList())
            ).isEmpty()
            ) {
                // Get all "loadable" (not actually being loaded!) extensions and put them in the sorted list.
                for (var entry : loadableExtensions) {
                    // Add to sorted list.
                    sortedList.add(entry.getKey());
                    // Remove to make the next iterations a little quicker (hopefully) and to find cyclic dependencies.
                    dependencyMap.remove(entry.getKey());

                    // Remove this dependency from all the lists (if they include it) to make way for next level of extensions.
                    for (var dependencies : dependencyMap.values()) {
                        dependencies.remove(entry.getKey());
                    }
                }
            }
        }

        // Check if there are cyclic extensions.
        if (!dependencyMap.isEmpty()) {
            LOGGER.error("Minestom found {} cyclic extensions.", dependencyMap.size());
            LOGGER.error("Cyclic extensions depend on each other and can therefore not be loaded.");
            for (var entry : dependencyMap.entrySet()) {
                DiscoveredExtension discoveredExtension = entry.getKey();
                LOGGER.error("{} could not be loaded, as it depends on: {}.",
                        discoveredExtension.getName(),
                        entry.getValue().stream().map(DiscoveredExtension::getName).collect(Collectors.joining(", ")));
            }

        }

        return sortedList;
    }

    /**
     * Checks if this list of extensions are loaded
     *
     * @param extensions The list of extensions to check against.
     * @return If all of these extensions are loaded.
     */
    private boolean isLoaded(@NotNull List<DiscoveredExtension> extensions) {
        return
                extensions.isEmpty() // Don't waste CPU on checking an empty array
                        // Make sure the internal extensions list contains all of these.
                        || extensions.stream().allMatch(ext -> this.extensions.containsKey(ext.getName().toLowerCase()));
    }

    private void loadDependencies(@NotNull List<DiscoveredExtension> extensions) {
        List<DiscoveredExtension> allLoadedExtensions = new LinkedList<>(extensions);

        for (Extension extension : immutableExtensions.values())
            allLoadedExtensions.add(extension.getOrigin());

        ExtensionDependencyResolver extensionDependencyResolver = new ExtensionDependencyResolver(allLoadedExtensions);

        for (DiscoveredExtension discoveredExtension : extensions) {
            try {
                DependencyGetter getter = new DependencyGetter();
                DiscoveredExtension.ExternalDependencies externalDependencies = discoveredExtension.getExternalDependencies();
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

                for (String artifact : externalDependencies.artifacts) {
                    var resolved = getter.get(artifact, dependenciesFolder);
                    addDependencyFile(resolved, discoveredExtension);
                    LOGGER.trace("Dependency of extension {}: {}", discoveredExtension.getName(), resolved);
                }

                for (String dependencyName : discoveredExtension.getDependencies()) {
                    var resolved = getter.get(dependencyName, dependenciesFolder);
                    addDependencyFile(resolved, discoveredExtension);
                    LOGGER.trace("Dependency of extension {}: {}", discoveredExtension.getName(), resolved);
                }
            } catch (Exception e) {
                discoveredExtension.loadStatus = DiscoveredExtension.LoadStatus.MISSING_DEPENDENCIES;
                LOGGER.error("Failed to load dependencies for extension {}", discoveredExtension.getName());
                LOGGER.error("Extension '{}' will not be loaded", discoveredExtension.getName());
                LOGGER.error("This is the exception", e);
            }
        }
    }

    private void addDependencyFile(@NotNull ResolvedDependency dependency, @NotNull DiscoveredExtension extension) {
        URL location = dependency.getContentsLocation();
        extension.files.add(location);
        LOGGER.trace("Added dependency {} to extension {} classpath", location.toExternalForm(), extension.getName());

        // recurse to add full dependency tree
        if (!dependency.getSubdependencies().isEmpty()) {
            LOGGER.trace("Dependency {} has subdependencies, adding...", location.toExternalForm());
            for (ResolvedDependency sub : dependency.getSubdependencies()) {
                addDependencyFile(sub, extension);
            }
            LOGGER.trace("Dependency {} has had its subdependencies added.", location.toExternalForm());
        }
    }

    @NotNull
    public File getExtensionFolder() {
        return extensionFolder;
    }

    public @NotNull Path getExtensionDataRoot() {
        return extensionDataRoot;
    }

    public void setExtensionDataRoot(@NotNull Path dataRoot) {
        this.extensionDataRoot = dataRoot;
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

    /**
     * Extensions are allowed to apply Mixin transformers, the magic happens here.
     */
    private void setupCodeModifiers(@NotNull List<DiscoveredExtension> extensions) {
        final ClassLoader cl = getClass().getClassLoader();
        if (!(cl instanceof MinestomRootClassLoader modifiableClassLoader)) {
            LOGGER.warn("Current class loader is not a MinestomOverwriteClassLoader, but {}. " +
                    "This disables code modifiers (Mixin support is therefore disabled). " +
                    "This can be fixed by starting your server using Bootstrap#bootstrap (optional).", cl);
            return;
        }
        setupCodeModifiers(extensions, modifiableClassLoader);
    }

    private void setupCodeModifiers(@NotNull List<DiscoveredExtension> extensions, MinestomRootClassLoader modifiableClassLoader) {
        LOGGER.info("Start loading code modifiers...");
        for (DiscoveredExtension extension : extensions) {
            try {
                for (String codeModifierClass : extension.getCodeModifiers()) {
                    boolean loaded = modifiableClassLoader.loadModifier(extension.files.toArray(new URL[0]), codeModifierClass);
                    if (!loaded) {
                        extension.addMissingCodeModifier(codeModifierClass);
                    }
                }
                if (!extension.getMixinConfig().isEmpty()) {
                    final String mixinConfigFile = extension.getMixinConfig();
                    try {
                        Mixins.addConfiguration(mixinConfigFile);
                        LOGGER.info("Found mixin in extension {}: {}", extension.getName(), mixinConfigFile);
                    } catch (ServiceNotAvailableError | MixinError | MixinException e) {
                        if (MinecraftServer.getExceptionManager() != null) {
                            MinecraftServer.getExceptionManager().handleException(e);
                        } else {
                            e.printStackTrace();
                        }
                        LOGGER.error("Could not load Mixin configuration: " + mixinConfigFile);
                        extension.setFailedToLoadMixinFlag();
                    }
                }
            } catch (Exception e) {
                if (MinecraftServer.getExceptionManager() != null) {
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

    private void unload(@NotNull Extension ext) {
        ext.preTerminate();
        ext.terminate();
        // remove callbacks for this extension
        String extensionName = ext.getOrigin().getName();
        ext.triggerChange(observer -> observer.onExtensionUnload(extensionName));
        // TODO: more callback types

        // Remove event node
        EventNode<Event> eventNode = ext.getEventNode();
        MinecraftServer.getGlobalEventHandler().removeChild(eventNode);

        ext.postTerminate();
        ext.unload();

        // remove as dependent of other extensions
        // this avoids issues where a dependent extension fails to reload, and prevents the base extension to reload too
        for (Extension e : extensions.values()) {
            e.getDependents().remove(ext.getOrigin().getName());
        }

        String id = ext.getOrigin().getName().toLowerCase();
        // remove from loaded extensions
        extensions.remove(id);

        // remove class loader, required to reload the classes
        MinestomExtensionClassLoader classloader = ext.getOrigin().removeMinestomExtensionClassLoader();
        try {
            // close resources
            classloader.close();
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        MinestomRootClassLoader.getInstance().removeChildInHierarchy(classloader);
    }

    public boolean reload(@NotNull String extensionName) {
        Extension ext = extensions.get(extensionName.toLowerCase());
        if (ext == null) {
            throw new IllegalArgumentException("Extension " + extensionName + " is not currently loaded.");
        }

        File originalJar = ext.getOrigin().getOriginalJar();
        if (originalJar == null) {
            LOGGER.error("Cannot reload extension {} that is not from a .jar file!", extensionName);
            return false;
        }

        LOGGER.info("Reload extension {} from jar file {}", extensionName, originalJar.getAbsolutePath());
        List<String> dependents = new LinkedList<>(ext.getDependents()); // copy dependents list
        List<File> originalJarsOfDependents = new LinkedList<>();

        for (String dependentID : dependents) {
            Extension dependentExt = extensions.get(dependentID.toLowerCase());
            File dependentOriginalJar = dependentExt.getOrigin().getOriginalJar();
            originalJarsOfDependents.add(dependentOriginalJar);
            if (dependentOriginalJar == null) {
                LOGGER.error("Cannot reload extension {} that is not from a .jar file!", dependentID);
                return false;
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

        return true;
    }

    public boolean loadDynamicExtension(@NotNull File jarFile) throws FileNotFoundException {
        if (!jarFile.exists()) {
            throw new FileNotFoundException("File '" + jarFile.getAbsolutePath() + "' does not exists. Cannot load extension.");
        }

        LOGGER.info("Discover dynamic extension from jar {}", jarFile.getAbsolutePath());
        DiscoveredExtension discoveredExtension = discoverFromJar(jarFile);
        List<DiscoveredExtension> extensionsToLoad = Collections.singletonList(discoveredExtension);
        return loadExtensionList(extensionsToLoad);
    }

    private boolean loadExtensionList(@NotNull List<DiscoveredExtension> extensionsToLoad) {
        // ensure correct order of dependencies
        LOGGER.debug("Reorder extensions to ensure proper load order");
        extensionsToLoad = generateLoadOrder(extensionsToLoad);
        loadDependencies(extensionsToLoad);

        // setup new classloaders for the extensions to reload
        for (DiscoveredExtension toReload : extensionsToLoad) {
            LOGGER.debug("Setting up classloader for extension {}", toReload.getName());
            toReload.setMinestomExtensionClassLoader(toReload.makeClassLoader());
        }

        // setup code modifiers for these extensions
        // TODO: it is possible the new modifiers cannot be applied (because the targeted classes are already loaded), should we issue a warning?
        setupCodeModifiers(extensionsToLoad);

        List<Extension> newExtensions = new LinkedList<>();
        for (DiscoveredExtension toReload : extensionsToLoad) {
            // reload extensions
            LOGGER.info("Actually load extension {}", toReload.getName());
            Extension loadedExtension = loadExtension(toReload);
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

    public void unloadExtension(@NotNull String extensionName) {
        Extension ext = extensions.get(extensionName.toLowerCase());

        if (ext == null) {
            throw new IllegalArgumentException("Extension " + extensionName + " is not currently loaded.");
        }

        List<String> dependents = new LinkedList<>(ext.getDependents()); // copy dependents list

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
        for (Extension extension : getExtensions()) {
            extension.unload();
        }
    }

    /**
     * Loads code modifiers early, that is before <code>MinecraftServer.init()</code> is called.
     */
    public static void loadCodeModifiersEarly() {
        // allow users to disable early code modifier load
        if ("true".equalsIgnoreCase(System.getProperty(DISABLE_EARLY_LOAD_SYSTEM_KEY))) {
            return;
        }
        LOGGER.info("Early load of code modifiers from extensions.");
        ExtensionManager manager = new ExtensionManager();

        // discover extensions that are present
        List<DiscoveredExtension> discovered = manager.discoverExtensions();

        // setup extension class loaders, so that Mixin can load the json configuration file correctly
        for (DiscoveredExtension e : discovered) {
            e.setMinestomExtensionClassLoader(e.makeClassLoader());
        }

        // setup code modifiers and mixins
        manager.setupCodeModifiers(discovered, MinestomRootClassLoader.getInstance());

        // setup is done, remove all extension classloaders
        for (Extension extension : manager.getExtensions()) {
            MinestomRootClassLoader.getInstance().removeChildInHierarchy(extension.getOrigin().getMinestomExtensionClassLoader());
        }
        LOGGER.info("Early load of code modifiers from extensions done!");
    }

    /**
     * Unloads all extensions
     */
    public void unloadAllExtensions() {
        // copy names, as the extensions map will be modified via the calls to unload
        Set<String> extensionNames = new HashSet<>(extensions.keySet());
        for (String ext : extensionNames) {
            if (extensions.containsKey(ext)) { // is still loaded? Because extensions can depend on one another, it might have already been unloaded
                unloadExtension(ext);
            }
        }
    }
}
