package net.minestom.server.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class MetadataDefImpl {
    static final Map<String, Integer> MAX_INDEX = new HashMap<>();

    static <T> MetadataDef.Entry.Index<T> index(int index, Function<T, Metadata.Entry<T>> function, T defaultValue) {
        final String caller = caller();
        storeMaxIndex(caller, index);
        final int superIndex = findSuperIndex(caller);
        return new MetadataDef.Entry.Index<>(superIndex + index, function, defaultValue);
    }

    static MetadataDef.Entry.Mask mask(int index, int bitMask, boolean defaultValue) {
        final String caller = caller();
        storeMaxIndex(caller, index);
        final int superIndex = findSuperIndex(caller);
        return new MetadataDef.Entry.Mask(superIndex + index, bitMask, defaultValue);
    }

    static <T extends MetadataDef> int count(Class<T> clazz) {
        final String name = clazz.getName();
        try {
            // Force load the class to ensure entries are registered
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        final int classIndex = MAX_INDEX.get(name);
        final int superIndex = findSuperIndex(name);
        return classIndex + superIndex + 1;
    }

    private static String caller() {
        return Thread.currentThread().getStackTrace()[3].getClassName();
    }

    static void storeMaxIndex(String caller, int index) {
        final int currentMax = MAX_INDEX.getOrDefault(caller, 0);
        MAX_INDEX.put(caller, Math.max(currentMax, index));
    }

    static int findSuperIndex(String caller) {
        try {
            final Class<?> subclass = Class.forName(caller);
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
