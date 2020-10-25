package net.minestom.server.extensions;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.minestom.dependencies.DependencyGetter;
import net.minestom.dependencies.maven.MavenRepository;
import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        final List<DiscoveredExtension> discoveredExtensions = discoverExtensions();
        loadDependencies(discoveredExtensions);
        // remove invalid extensions
        discoveredExtensions.removeIf(ext -> ext.loadStatus != DiscoveredExtension.LoadStatus.LOAD_SUCCESS);
        setupCodeModifiers(discoveredExtensions);

        for (DiscoveredExtension discoveredExtension : discoveredExtensions) {
            URLClassLoader loader;
            URL[] urls = new URL[discoveredExtension.files.length];
            try {
                for (int i = 0; i < urls.length; i++) {
                    urls[i] = discoveredExtension.files[i].toURI().toURL();
                }
                loader = newClassLoader(urls);
            } catch (MalformedURLException e) {
                log.error("Failed to get URL.", e);
                continue;
            }

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

            extensions.put(extensionName.toLowerCase(), extension);
        }
    }

    private void loadDependencies(List<DiscoveredExtension> extensions) {
        for (DiscoveredExtension ext : extensions) {
            try {
                DependencyGetter getter = new DependencyGetter();
                DiscoveredExtension.Dependencies dependencies = ext.getDependencies();
                List<MavenRepository> repoList = new LinkedList<>();
                for (var repository : dependencies.repositories) {
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

                for (var artifact : dependencies.artifacts) {
                    var resolved = getter.get(artifact, dependenciesFolder);
                    injectIntoClasspath(resolved.getContentsLocation(), ext);
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

    private void injectIntoClasspath(URL dependency, DiscoveredExtension extension) {
        final ClassLoader cl = getClass().getClassLoader();
        if (!(cl instanceof URLClassLoader)) {
            throw new IllegalStateException("Current class loader is not a URLClassLoader, but " + cl + ". This prevents adding URLs into the classpath at runtime.");
        }
        try {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(cl, dependency);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to inject URL " + dependency + " into classpath. From extension " + extension.getName(), e);
        }
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
                extension.files = new File[]{file};

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
                extension.files = new File[]{new File(extensionClasses), new File(extensionResources)};

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

    /**
     * Loads a URL into the classpath.
     *
     * @param urls {@link URL} (usually a JAR) that should be loaded.
     */
    @NotNull
    public URLClassLoader newClassLoader(@NotNull URL[] urls) {
        return URLClassLoader.newInstance(urls, ExtensionManager.class.getClassLoader());
    }

    @NotNull
    public File getExtensionFolder() {
        return extensionFolder;
    }

    @NotNull
    public List<Extension> getExtensions() {
        return new ArrayList<>(extensions.values());
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
        if (!(cl instanceof MinestomOverwriteClassLoader)) {
            log.warn("Current class loader is not a MinestomOverwriteClassLoader, but " + cl + ". This disables code modifiers (Mixin support is therefore disabled)");
            return;
        }
        MinestomOverwriteClassLoader modifiableClassLoader = (MinestomOverwriteClassLoader) cl;
        log.info("Start loading code modifiers...");
        for (DiscoveredExtension extension : extensions) {
            try {
                for (String codeModifierClass : extension.getCodeModifiers()) {
                    modifiableClassLoader.loadModifier(extension.files, codeModifierClass);
                }
                if (!extension.getMixinConfig().isEmpty()) {
                    final String mixinConfigFile = extension.getMixinConfig();
                    Mixins.addConfiguration(mixinConfigFile);
                    log.info("Found mixin in extension " + extension.getName() + ": " + mixinConfigFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Failed to load code modifier for extension in files: " + Arrays.toString(extension.files), e);
            }
        }
        log.info("Done loading code modifiers.");
    }


}
