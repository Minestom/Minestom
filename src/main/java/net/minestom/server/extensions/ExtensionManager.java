package net.minestom.server.extensions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExtensionManager {
    private final Map<URL, URLClassLoader> extensionLoaders = new HashMap<>();
    private final Map<String, Extension> extensions = new HashMap<>();
    private final File extensionFolder = new File("extensions");

    public ExtensionManager() {
    }

    public void loadExtensionJARs() {
        if (!extensionFolder.exists()) {
            if (!extensionFolder.mkdirs()) {
                log.error("Could not find or create the extension folder, extensions will not be loaded!");
                return;
            }
        }

        for (File file : extensionFolder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            URLClassLoader loader;
            try {
                URL url = file.toURI().toURL();
                loader = loadJar(url);
                extensionLoaders.put(url, loader);
            } catch (MalformedURLException e) {
                log.error(String.format("Failed to get URL for file %s.", file.getPath()));
                return;
            }
            InputStream extensionInputStream = loader.getResourceAsStream("extension.json");
            if (extensionInputStream == null) {
                log.error(String.format("Failed to find extension.json in the file '%s'.", file.getPath()));
                return;
            }
            JsonObject extensionDescription = JsonParser.parseReader(new InputStreamReader(extensionInputStream)).getAsJsonObject();

            String mainClass = extensionDescription.get("entrypoint").getAsString();
            String extensionName = extensionDescription.get("name").getAsString();
            
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

    /**
     * Loads a URL into the classpath.
     *
     * @param url {@link URL} (usually a JAR) that should be loaded.
     */
    @NotNull
    public URLClassLoader loadJar(@NotNull URL url) {
        return URLClassLoader.newInstance(new URL[]{url}, ExtensionManager.class.getClassLoader());
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
    public Map<URL, URLClassLoader> getExtensionLoaders() {
        return new HashMap<>(extensionLoaders);
    }
}
