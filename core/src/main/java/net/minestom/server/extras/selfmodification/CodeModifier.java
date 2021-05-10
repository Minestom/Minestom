package net.minestom.server.extras.selfmodification;

import org.objectweb.asm.tree.ClassNode;

/**
 * Will be called by {@link MinestomRootClassLoader} to transform classes at load-time
 */
public abstract class CodeModifier {
    /**
     * Must return true iif the class node has been modified
     * @param source
     * @return
     */
    public abstract boolean transform(ClassNode source);

    /**
     * Beginning of the class names to transform.
     * 'null' is allowed to transform any class, but not recommended
     * @return
     */
    public abstract String getNamespace();
}
