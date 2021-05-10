package net.minestom.server.extras.selfmodification.mixins;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.spongepowered.asm.service.IClassProvider;

import java.net.URL;

/**
 * Provides classes for Mixin
 */
public class MinestomClassProvider implements IClassProvider {
    private final MinestomRootClassLoader classLoader;

    public MinestomClassProvider(MinestomRootClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public URL[] getClassPath() {
        return classLoader.getURLs();
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return classLoader.findClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, classLoader);
    }
}
