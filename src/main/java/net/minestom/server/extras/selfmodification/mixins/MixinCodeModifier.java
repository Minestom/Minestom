package net.minestom.server.extras.selfmodification.mixins;

import net.minestom.server.extras.selfmodification.CodeModifier;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.MixinProcessor;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;
import org.spongepowered.asm.service.ISyntheticClassInfo;
import org.spongepowered.asm.service.ISyntheticClassRegistry;
import org.spongepowered.asm.transformers.TreeTransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MixinCodeModifier extends CodeModifier {

    private Method transformClassMethod;
    private TreeTransformer processor;

    public MixinCodeModifier() {
        try {
            Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
            Constructor<?> ctor = mixinTransformerClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            this.processor = (TreeTransformer) ctor.newInstance();
            transformClassMethod = mixinTransformerClass.getDeclaredMethod("transformClass", MixinEnvironment.class, String.class, ClassNode.class);
            transformClassMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean transform(ClassNode source) {
        try {
            return (boolean) transformClassMethod.invoke(processor, MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT), source.name.replace("/", "."), source);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getNamespace() {
        return null;
    }
}
