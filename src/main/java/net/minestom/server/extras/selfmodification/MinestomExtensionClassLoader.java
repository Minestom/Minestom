package net.minestom.server.extras.selfmodification;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MinestomExtensionClassLoader extends HierarchyClassLoader {
    /**
     * Root ClassLoader, everything goes through it before any attempt at loading is done inside this classloader
     */
    private final MinestomRootClassLoader root;

    /**
     * Main of the main class of the extension linked to this classloader
     */
    private final String mainClassName;

    public MinestomExtensionClassLoader(String extensionName, String mainClassName, URL[] urls, MinestomRootClassLoader root) {
        super(extensionName, urls, root);
        this.root = root;
        this.mainClassName = mainClassName;
    }

    /**
     * Returns the name of the extension linked to this classloader
     * @return the name of the extension linked to this classloader
     */
    public String getExtensionName() {
        // simply calls ClassLoader#getName as the extension name is used to name this classloader
        //  this method is simply for ease-of-use
        return getName();
    }

    /**
     * Returns the main class name linked to the extension responsible for this classloader.
     * Used by the root classloader to let extensions load themselves in a dev environment.
     * @return the main class name linked to the extension responsible for this classloader
     */
    public String getMainClassName() {
        return mainClassName;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return root.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return root.loadClass(name, resolve);
    }

    /**
     * Assumes the name is not null, nor it does represent a protected class
     * @param name
     * @return
     * @throws ClassNotFoundException if the class is not found inside this classloader
     */
    public Class<?> loadClassAsChild(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if(loadedClass != null) {
            return loadedClass;
        }

        try {
            // not in children, attempt load in this classloader
            String path = name.replace(".", "/") + ".class";
            InputStream in = getResourceAsStream(path);
            if (in == null) {
                throw new ClassNotFoundException("Could not load class " + name);
            }
            try (in) {
                byte[] bytes = in.readAllBytes();
                bytes = root.transformBytes(bytes, name);
                Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } catch (IOException e) {
                throw new ClassNotFoundException("Could not load class " + name, e);
            }
        } catch (ClassNotFoundException e) {
            for(MinestomExtensionClassLoader child : children) {
                try {
                    return child.loadClassAsChild(name, resolve);
                } catch (ClassNotFoundException e1) {
                    // move on to next child
                }
            }
            throw e;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.err.println("Class loader "+getName()+" finalized.");
    }

    /**
     * Is the given class name the name of the entry point of one the extensions from this classloader chain?
     * @param name the class name to check
     * @return whether the given class name the name of the entry point of one the extensions from this classloader chain
     * @see MinestomRootClassLoader#loadBytes(String, boolean) for more information
     */
    protected boolean isMainExtensionClass(String name) {
        if(mainClassName.equals(name))
            return true;
        return children.stream().anyMatch(c -> c.isMainExtensionClass(name));
    }
}
