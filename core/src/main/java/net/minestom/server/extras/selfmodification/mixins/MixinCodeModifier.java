package net.minestom.server.extras.selfmodification.mixins;

import net.minestom.server.extras.selfmodification.CodeModifier;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.transformers.TreeTransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * CodeModifier responsible for applying Mixins during class load
 */
public class MixinCodeModifier extends CodeModifier {

    /**
     * Call MixinTransformer's transformClass
     */
    private final Method transformClassMethod;
    private final TreeTransformer transformer;

    public MixinCodeModifier() {
        try {
            // MixinTransformer is package-protected, so we have to force to gain access
            Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
            Constructor<?> ctor = mixinTransformerClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            this.transformer = (TreeTransformer) ctor.newInstance();

            // we can't access the MixinTransformer type here, so we use reflection to access the method
            transformClassMethod = mixinTransformerClass.getDeclaredMethod("transformClass", MixinEnvironment.class, String.class, ClassNode.class);
            transformClassMethod.setAccessible(true);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialize MixinCodeModifier", e);
        }
    }

    @Override
    public boolean transform(ClassNode source) {
        try {
            return (boolean) transformClassMethod.invoke(transformer, MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT), source.name.replace("/", "."), source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getNamespace() {
        return null;
    }
}
