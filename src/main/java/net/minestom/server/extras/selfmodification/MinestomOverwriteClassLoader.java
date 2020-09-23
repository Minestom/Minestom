package net.minestom.server.extras.selfmodification;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class Loader that can modify class bytecode when they are loaded
 */
@Slf4j
public class MinestomOverwriteClassLoader extends URLClassLoader {

    private static MinestomOverwriteClassLoader INSTANCE;

    /**
     * Classes that cannot be loaded/modified by this classloader.
     * Will go through parent class loader
     */
    public final Set<String> protectedClasses = new HashSet<>() {
        {
            add("net.minestom.server.extras.selfmodification.CodeModifier");
            add("net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader");
        }
    };
    public final Set<String> protectedPackages = new HashSet<>() {
        {
            add("com.google");
            add("com.mojang");
            add("org.objectweb.asm");
            add("org.slf4j");
            add("org.apache");
            add("org.spongepowered");
            add("net.minestom.server.extras.selfmodification");
        }
    };
    /**
     * Used to let ASM find out common super types, without actually commiting to loading them
     * Otherwise ASM would accidentally load classes we might want to modify
     */
    private final URLClassLoader asmClassLoader;

    // TODO: replace by tree to optimize lookup times. We can use the fact that package names are split with '.' to allow for fast lookup
    // TODO: eg. Node("java", Node("lang"), Node("io")). Loading "java.nio.Channel" would apply modifiers from "java", but not "java.io" or "java.lang".
    // TODO: that's an example, please don't modify standard library classes. And this classloader should not let you do it because it first asks the platform classloader

    // TODO: priorities?
    private final List<CodeModifier> modifiers = new LinkedList<>();

    private MinestomOverwriteClassLoader(ClassLoader parent) {
        super("Minestom ClassLoader", extractURLsFromClasspath(), parent);
        asmClassLoader = newChild(new URL[0]);
    }

    public static MinestomOverwriteClassLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (MinestomOverwriteClassLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MinestomOverwriteClassLoader(MinestomOverwriteClassLoader.class.getClassLoader());
                }
            }
        }
        return INSTANCE;
    }

    private static URL[] extractURLsFromClasspath() {
        String classpath = System.getProperty("java.class.path");
        String[] parts = classpath.split(";");
        URL[] urls = new URL[parts.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                String part = parts[i];
                String protocol;
                if (part.contains("!")) {
                    protocol = "jar://";
                } else {
                    protocol = "file://";
                }
                urls[i] = new URL(protocol + part);
            } catch (MalformedURLException e) {
                throw new Error(e);
            }
        }
        return urls;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null)
            return loadedClass;

        try {
            // we do not load system classes by ourselves
            Class<?> systemClass = ClassLoader.getPlatformClassLoader().loadClass(name);
            log.trace("System class: " + systemClass);
            return systemClass;
        } catch (ClassNotFoundException e) {
            try {
                if (isProtected(name)) {
                    log.trace("Protected: " + name);
                    return super.loadClass(name, resolve);
                }

                return define(name, loadBytes(name, true), resolve);
            } catch (Exception ex) {
                log.trace("Fail to load class, resorting to parent loader: " + name, ex);
                // fail to load class, let parent load
                // this forbids code modification, but at least it will load
                return super.loadClass(name, resolve);
            }
        }
    }

    private boolean isProtected(String name) {
        if (!protectedClasses.contains(name)) {
            for (String start : protectedPackages) {
                if (name.startsWith(start))
                    return true;
            }
            return false;
        }
        return true;
    }

    private Class<?> define(String name, byte[] bytes, boolean resolve) throws ClassNotFoundException {
        Class<?> defined = defineClass(name, bytes, 0, bytes.length);
        log.trace("Loaded with code modifiers: " + name);
        if (resolve) {
            resolveClass(defined);
        }
        return defined;
    }

    /**
     * Loads and possibly transforms class bytecode corresponding to the given binary name.
     *
     * @param name
     * @param transform
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public byte[] loadBytes(String name, boolean transform) throws IOException, ClassNotFoundException {
        if (name == null)
            throw new ClassNotFoundException();
        String path = name.replace(".", "/") + ".class";
        byte[] bytes = getResourceAsStream(path).readAllBytes();
        if (transform && !isProtected(name)) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            boolean modified = false;
            synchronized (modifiers) {
                for (CodeModifier modifier : modifiers) {
                    boolean shouldModify = modifier.getNamespace() == null || name.startsWith(modifier.getNamespace());
                    if (shouldModify) {
                        modified |= modifier.transform(node);
                    }
                }
            }
            if (modified) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
                    @Override
                    protected ClassLoader getClassLoader() {
                        return asmClassLoader;
                    }
                };
                node.accept(writer);
                bytes = writer.toByteArray();
                log.trace("Modified " + name);
            }
        }
        return bytes;
    }

    // overriden to increase access (from protected to public)
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @NotNull
    public URLClassLoader newChild(@NotNull URL[] urls) {
        return URLClassLoader.newInstance(urls, this);
    }

    public void loadModifier(File[] originFiles, String codeModifierClass) {
        URL[] urls = new URL[originFiles.length];
        try {
            for (int i = 0; i < originFiles.length; i++) {
                urls[i] = originFiles[i].toURI().toURL();
            }
            URLClassLoader loader = newChild(urls);
            Class<?> modifierClass = loader.loadClass(codeModifierClass);
            if (CodeModifier.class.isAssignableFrom(modifierClass)) {
                CodeModifier modifier = (CodeModifier) modifierClass.getDeclaredConstructor().newInstance();
                synchronized (modifiers) {
                    log.warn("Added Code modifier: " + modifier);
                    addCodeModifier(modifier);
                }
            }
        } catch (MalformedURLException | ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void addCodeModifier(CodeModifier modifier) {
        synchronized (modifiers) {
            modifiers.add(modifier);
        }
    }

    public List<CodeModifier> getModifiers() {
        return modifiers;
    }
}
