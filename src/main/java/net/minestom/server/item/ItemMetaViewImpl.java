package net.minestom.server.item;

import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class ItemMetaViewImpl {
    static <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> Class<V> viewType(Class<T> metaClass) {
        final Type type = metaClass.getGenericInterfaces()[0];
        return (Class<V>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    static <T extends ItemMetaView<?>> T construct(Class<T> metaClass, TagReadable tagReadable) {
        try {
            final Constructor<T> cons = metaClass.getDeclaredConstructor(TagReadable.class);
            return cons.newInstance(tagReadable);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> V constructBuilder(Class<T> metaClass, TagHandler tagHandler) {
        final Class<V> clazz = viewType(metaClass);
        try {
            final Constructor<V> cons = clazz.getDeclaredConstructor(TagHandler.class);
            return cons.newInstance(tagHandler);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
