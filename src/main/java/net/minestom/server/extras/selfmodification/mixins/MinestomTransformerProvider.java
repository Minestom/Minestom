package net.minestom.server.extras.selfmodification.mixins;

import net.minestom.server.extras.selfmodification.CodeModifier;
import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.ITreeClassTransformer;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MinestomTransformerProvider implements ITransformerProvider {
    private final MinestomOverwriteClassLoader classLoader;
    private List<ITransformer> transformers;

    public MinestomTransformerProvider(MinestomOverwriteClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void addTransformerExclusion(String name) {
        classLoader.protectedClasses.add(name);
    }

    @Override
    public Collection<ITransformer> getTransformers() {
        return getDelegatedTransformers();
    }

    @Override
    public Collection<ITransformer> getDelegatedTransformers() {
        if(transformers == null) {
            transformers = buildTransformerList();
        }
        return transformers;
    }

    private List<ITransformer> buildTransformerList() {
        List<ITransformer> result = new LinkedList<>();
        for(CodeModifier modifier : classLoader.getModifiers()) {
            result.add(toMixin(modifier));
        }

        try {
            Class<?> clazz = classLoader.loadClass("org.spongepowered.asm.mixin.transformer.MixingTransformer");
            ITransformer mixinTransformer = (ITransformer) clazz.getDeclaredConstructor().newInstance();
            result.add(mixinTransformer);
        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    private ITransformer toMixin(CodeModifier modifier) {
        return new ITreeClassTransformer() {
            @Override
            public boolean transformClassNode(String name, String transformedName, ClassNode classNode) {
                return modifier.transform(classNode);
            }

            @Override
            public String getName() {
                return modifier.getClass().getName();
            }

            @Override
            public boolean isDelegationExcluded() {
                return false;
            }
        };
    }
}
