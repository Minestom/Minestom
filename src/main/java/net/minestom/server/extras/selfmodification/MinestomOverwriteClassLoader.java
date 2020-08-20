package net.minestom.server.extras.selfmodification;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

// TODO: register code modifiers
public class MinestomOverwriteClassLoader extends URLClassLoader {

    public MinestomOverwriteClassLoader(ClassLoader parent) {
        super("Minestom ClassLoader", loadURLs(), parent);
    }

    private static URL[] loadURLs() {
        String classpath = System.getProperty("java.class.path");
        String[] parts = classpath.split(";");
        URL[] urls = new URL[parts.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                String part = parts[i];
                String protocol;
                if(part.contains("!")) {
                    protocol = "jar://";
                } else {
                    protocol = "file://";
                }
                urls[i] = new URL(protocol+part);
            } catch (MalformedURLException e) {
                throw new Error(e);
            }
        }
        return urls;
    }

    private static URL[] fromParent(ClassLoader parent) {
        if(parent instanceof URLClassLoader) {
            return ((URLClassLoader) parent).getURLs();
        }
        return new URL[0];
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if(loadedClass != null)
            return loadedClass;

        try {
            Class<?> systemClass = ClassLoader.getPlatformClassLoader().loadClass(name);
            return systemClass;
        } catch (ClassNotFoundException e) {
            try {
                String path = name.replace(".", "/") + ".class";
                byte[] bytes = getResourceAsStream(path).readAllBytes();
                Class<?> defined = defineClass(name, bytes, 0, bytes.length);
                if(resolve) {
                    resolveClass(defined);
                }
                return defined;
            } catch (Exception ioException) {
                // fail to load class, let parent load
                // this forbids code modification, but at least it will load
                return super.loadClass(name, resolve);
            }
        }
    }

    public void loadModifier(File originFile, String codeModifierClass) {
        throw new UnsupportedOperationException("TODO");
    }
}
