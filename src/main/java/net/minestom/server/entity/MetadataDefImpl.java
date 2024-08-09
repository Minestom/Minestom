package net.minestom.server.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class MetadataDefImpl {
    static final Map<String, Integer> MAX_INDEX = new HashMap<>();

    static <T> MetadataDef.Entry<T> index(int index, Function<T, Metadata.Entry<T>> function, T defaultValue) {
        storeMaxIndex(index);
        final int superIndex = findSuperIndex();
        return new MetadataDef.Entry.Index<>(superIndex + index, function, defaultValue);
    }

    static <T> MetadataDef.Entry<Boolean> mask(int index, int bitMask, boolean defaultValue) {
        storeMaxIndex(index);
        final int superIndex = findSuperIndex();
        return new MetadataDef.Entry.Mask(superIndex + index, bitMask, defaultValue);
    }

    static void storeMaxIndex(int index) {
        final String className = Thread.currentThread().getStackTrace()[3].getClassName();
        final int currentMax = MAX_INDEX.getOrDefault(className, 0);
        MAX_INDEX.put(className, Math.max(currentMax, index));
    }

    static int findSuperIndex() {
        try {
            final String className = Thread.currentThread().getStackTrace()[3].getClassName();
            final Class<?> subclass = Class.forName(className);
            Class<?> superclass = subclass.getSuperclass();
            if (superclass == Object.class) return 0;

            int index = 0;
            do {
                index += MAX_INDEX.get(superclass.getName()) + 1;
                superclass = superclass.getSuperclass();
            } while (superclass != Object.class);

            return index;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
