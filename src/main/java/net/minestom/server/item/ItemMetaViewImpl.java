package net.minestom.server.item;

import net.minestom.server.item.component.ItemComponentPatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Deprecated
final class ItemMetaViewImpl {
    static <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> Class<V> viewType(Class<T> metaClass) {
        final Type type = metaClass.getGenericInterfaces()[0];
        return (Class<V>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    static <T extends ItemMetaView<?>> T construct(Class<T> metaClass, ItemComponentPatch components) {
        try {
            final Constructor<T> cons = metaClass.getDeclaredConstructor(ItemComponentPatch.class);
            return cons.newInstance(components);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> V constructBuilder(Class<T> metaClass, ItemComponentPatch.Builder components) {
        final Class<V> clazz = viewType(metaClass);
        try {
            final Constructor<V> cons = clazz.getDeclaredConstructor(ItemComponentPatch.Builder.class);
            return cons.newInstance(components);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
