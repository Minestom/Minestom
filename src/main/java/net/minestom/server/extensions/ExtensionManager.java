package net.minestom.server.extensions;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixins;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipFile;

@Slf4j
public class ExtensionManager {
    private final Map<String, URLClassLoader> extensionLoaders = new HashMap<>();
    private final Map<String, Extension> extensions = new HashMap<>();
    private final File extensionFolder = new File("extensions");
    private final static String INDEV_CLASSES_FOLDER = "minestom.extension.indevfolder.classes";
    private final static String INDEV_RESOURCES_FOLDER = "minestom.extension.indevfolder.resources";

    public ExtensionManager() {
    }

    public void loadExtensions() {
        if (!extensionFolder.exists()) {
            if (!extensionFolder.mkdirs()) {
                log.error("Could not find or create the extension folder, extensions will not be loaded!");
                return;
            }
        }

        List<DiscoveredExtension> discoveredExtensions = discoverExtensions();
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
                return;
            }
            InputStream extensionInputStream = loader.getResourceAsStream("extension.json");
            if (extensionInputStream == null) {
                StringBuilder urlsString = new StringBuilder();
                for (int i = 0; i < urls.length; i++) {
                    URL url = urls[i];
                    if(i != 0) {
                        urlsString.append(" ; ");
                    }
                    urlsString.append("'").append(url.toString()).append("'");
                }
                log.error(String.format("Failed to find extension.json in the urls '%s'.", urlsString));
                return;
            }
            JsonObject extensionDescription = JsonParser.parseReader(new InputStreamReader(extensionInputStream)).getAsJsonObject();

            String mainClass = extensionDescription.get("entrypoint").getAsString();
            String extensionName = extensionDescription.get("name").getAsString();

            extensionLoaders.put(extensionName, loader);

            if (extensions.containsKey(extensionName.toLowerCase())) {
                log.error(String.format("An extension called '%s' has already been registered.", extensionName));
                return;
            }

            Class<?> jarClass;
            try {
                jarClass = Class.forName(mainClass, true, loader);
            } catch (ClassNotFoundException e) {
                log.error(String.format("Could not find main class '%s' in extension '%s'.", mainClass, extensionName), e);
                return;
            }

            Class<? extends Extension> extensionClass;
            try {
                extensionClass = jarClass.asSubclass(Extension.class);
            } catch (ClassCastException e) {
                log.error(String.format("Main class '%s' in '%s' does not extend the 'extension superclass'.", mainClass, extensionName), e);
                return;
            }

            Constructor<? extends Extension> constructor;
            try {
                constructor = extensionClass.getDeclaredConstructor();
                // Let's just make it accessible, plugin creators don't have to make this public.
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                log.error(String.format("Main class '%s' in '%s' does not define a no-args constructor.", mainClass, extensionName), e);
                return;
            }
            Extension extension = null;
            try {
                // Is annotated with NotNull
                extension = constructor.newInstance();
            } catch (InstantiationException e) {
                log.error(String.format("Main class '%s' in '%s' cannot be an abstract class.", mainClass, extensionName), e);
                return;
            } catch (IllegalAccessException ignored) {
                // We made it accessible, should not occur
            } catch (InvocationTargetException e) {
                log.error(
                        String.format(
                                "While instantiating the main class '%s' in '%s' an exception was thrown.", mainClass, extensionName
                        ), e.getTargetException()
                );
                return;
            }
            // Set description
            try {
                Field descriptionField = extensionClass.getSuperclass().getDeclaredField("description");
                descriptionField.setAccessible(true);
                descriptionField.set(extension, extensionDescription);
            } catch (IllegalAccessException e) {
                // We made it accessible, should not occur
            } catch (NoSuchFieldException e) {
                log.error(String.format("Main class '%s' in '%s' has no description field.", mainClass, extensionName), e);
                return;
            }
            // Set Logger
            try {
                Field descriptionField = extensionClass.getSuperclass().getDeclaredField("logger");
                descriptionField.setAccessible(true);
                descriptionField.set(extension, LoggerFactory.getLogger(extensionClass));
            } catch (IllegalAccessException e) {
                // We made it accessible, should not occur
            } catch (NoSuchFieldException e) {
                log.error(String.format("Main class '%s' in '%s' has no logger field.", mainClass, extensionName), e);
            }

            extensions.put(extensionName.toLowerCase(), extension);
        }
    }

    private List<DiscoveredExtension> discoverExtensions() {
        Gson gson = new Gson();
        List<DiscoveredExtension> extensions = new LinkedList<>();
        for (File file : extensionFolder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            try(ZipFile f = new ZipFile(file);
                    InputStreamReader reader = new InputStreamReader(f.getInputStream(f.getEntry("extension.json")))) {

                DiscoveredExtension extension = new DiscoveredExtension();
                extension.files = new File[]{file};
                extension.description = gson.fromJson(reader, JsonObject.class);
                extensions.add(extension);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // this allows developers to have their extension discovered while working on it, without having to build a jar and put in the extension folder
        if(System.getProperty(INDEV_CLASSES_FOLDER) != null && System.getProperty(INDEV_RESOURCES_FOLDER) != null) {
            log.info("Found indev folders for extension. Adding to list of discovered extensions.");
            String extensionClasses = System.getProperty(INDEV_CLASSES_FOLDER);
            String extensionResources = System.getProperty(INDEV_RESOURCES_FOLDER);
            try(InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(extensionResources, "extension.json")))) {
                DiscoveredExtension extension = new DiscoveredExtension();
                extension.files = new File[] { new File(extensionClasses), new File(extensionResources) };
                extension.description = gson.fromJson(reader, JsonObject.class);
                extensions.add(extension);
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
     * Extensions are allowed to apply Mixin transformers, the magic happens here
     */
    private void setupCodeModifiers(List<DiscoveredExtension> extensions) {
        ClassLoader cl = getClass().getClassLoader();
        if(!(cl instanceof MinestomOverwriteClassLoader)) {
            log.warn("Current class loader is not a MinestomOverwriteClassLoader, but "+cl+". This disables code modifiers (Mixin support is therefore disabled)");
            return;
        }
        MinestomOverwriteClassLoader modifiableClassLoader = (MinestomOverwriteClassLoader)cl;
        log.info("Start loading code modifiers...");
        for(DiscoveredExtension extension : extensions) {
            try {
                if(extension.description.has("codeModifiers")) {
                    JsonArray codeModifierClasses = extension.description.getAsJsonArray("codeModifiers");
                    for(JsonElement elem : codeModifierClasses) {
                        modifiableClassLoader.loadModifier(extension.files, elem.getAsString());
                    }
                }
                if(extension.description.has("mixinConfig")) {
                    String mixinConfigFile = extension.description.get("mixinConfig").getAsString();
                    Mixins.addConfiguration(mixinConfigFile);
                    log.info("Found mixin in extension "+extension.description.get("name").getAsString()+": "+mixinConfigFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Failed to load code modifier for extension in files: "+Arrays.toString(extension.files), e);
            }
        }
        log.info("Done loading code modifiers.");
    }

    private class DiscoveredExtension {
        private File[] files;
        private JsonObject description;
    }
}
