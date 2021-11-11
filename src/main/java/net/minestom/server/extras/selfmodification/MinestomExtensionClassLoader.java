package net.minestom.server.extras.selfmodification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MinestomExtensionClassLoader extends HierarchyClassLoader {
    /**
     * Root ClassLoader, everything goes through it before any attempt at loading is done inside this classloader
     */
    private final ClassLoader root;

    /**
     * Main of the main class of the extension linked to this classloader
     */
    private final String mainClassName;

    private final Logger logger = LoggerFactory.getLogger(MinestomExtensionClassLoader.class);

    public MinestomExtensionClassLoader(String extensionName, String mainClassName, URL[] urls, ClassLoader root) {
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
        try {
            System.out.println("TRYING TO LOAD " + name + " IN " + getName());
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            System.out.println("COULD NOT LOAD, TRYING CHILDREN");
            for (MinestomExtensionClassLoader child : children) {
                try {
                    return child.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                    System.out.println("NOT FOUND IN " + child.getName() + " EITHER");
                }
            }
            throw e;
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            System.out.println("TRYING 2 LOAD " + name + " IN " + getName());
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            System.out.println("COULD NOT LOAD 2, TRYING CHILDREN");
            for (MinestomExtensionClassLoader child : children) {
                try {
                    return child.loadClass(name, resolve);
                } catch (ClassNotFoundException ignored) {
                    System.out.println("NOT FOUND IN " + child.getName() + " EITHER");
                }
            }
            throw e;
        }
    }

    /**
     * Assumes the name is not null, nor it does represent a protected class
     * @param name
     * @return
     * @throws ClassNotFoundException if the class is not found inside this classloader
     */
    public Class<?> loadClassAsChild(String name, boolean resolve) throws ClassNotFoundException {
        throw new RuntimeException("Cannot load " + name + " using old mechanism");
//        logger.info("Loading class " + name + " as child of " + getName());
//        Class<?> loadedClass = findLoadedClass(name);
//        if(loadedClass != null) {
//            logger.info("Found loaded class");
//            return loadedClass;
//        }
//
//        try {
//            // not in children, attempt load in this classloader
//            String path = name.replace(".", "/") + ".class";
//            InputStream in = getResourceAsStream(path);
//            if (in == null) {
//                throw new ClassNotFoundException("Could not load class " + name);
//            }
//            logger.info("Found in resources");
//            try (in) {
//                byte[] bytes = in.readAllBytes();
//                Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
//                if (resolve) {
//                    resolveClass(clazz);
//                }
//                return clazz;
//            } catch (IOException e) {
//                throw new ClassNotFoundException("Could not load class " + name, e);
//            }
//        } catch (ClassNotFoundException e) {
//            for(MinestomExtensionClassLoader child : children) {
//                try {
//                    return child.loadClassAsChild(name, resolve);
//                } catch (ClassNotFoundException e1) {
//                    // move on to next child
//                }
//            }
//            throw e;
//        }
    }

    /**
     * Is the given class name the name of the entry point of one the extensions from this classloader chain?
     * @param name the class name to check
     * @return whether the given class name the name of the entry point of one the extensions from this classloader chain
     * @see MinestomRootClassLoader#loadBytes(String, boolean) for more information
     */
    protected boolean isMainExtensionClass(String name) {

        if (mainClassName.equals(name))
            return true;

        for (MinestomExtensionClassLoader child : children) {
            if (child.isMainExtensionClass(name)) return true;
        }

        return false;
    }
}
