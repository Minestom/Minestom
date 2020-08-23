package net.minestom.server.extras.selfmodification.mixins;

import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;

import java.io.IOException;

/**
 * Provides class bytecode for Mixin
 */
public class MinestomBytecodeProvider implements IClassBytecodeProvider {
    private final MinestomOverwriteClassLoader classLoader;

    public MinestomBytecodeProvider(MinestomOverwriteClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, false);
    }

    private ClassNode loadNode(String name, boolean transform) throws ClassNotFoundException {
        ClassNode node = new ClassNode();
        ClassReader reader;
        try {
            reader = new ClassReader(classLoader.loadBytes(name, transform));
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load ClassNode with name "+name, e);
        }
        reader.accept(node, 0);
        return node;
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return loadNode(name, runTransformers);
    }
}
