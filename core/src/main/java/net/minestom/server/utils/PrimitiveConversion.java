package net.minestom.server.utils;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveConversion {

    private static Map<Class, Class> primitiveToBoxedTypeMap = new HashMap<>();

    static {
        // Primitive
        primitiveToBoxedTypeMap.put(boolean.class, Boolean.class);
        primitiveToBoxedTypeMap.put(byte.class, Byte.class);
        primitiveToBoxedTypeMap.put(char.class, Character.class);
        primitiveToBoxedTypeMap.put(short.class, Short.class);
        primitiveToBoxedTypeMap.put(int.class, Integer.class);
        primitiveToBoxedTypeMap.put(long.class, Long.class);
        primitiveToBoxedTypeMap.put(float.class, Float.class);
        primitiveToBoxedTypeMap.put(double.class, Double.class);

        // Primitive one dimension array
        primitiveToBoxedTypeMap.put(boolean[].class, Boolean[].class);
        primitiveToBoxedTypeMap.put(byte[].class, Byte[].class);
        primitiveToBoxedTypeMap.put(char[].class, Character[].class);
        primitiveToBoxedTypeMap.put(short[].class, Short[].class);
        primitiveToBoxedTypeMap.put(int[].class, Integer[].class);
        primitiveToBoxedTypeMap.put(long[].class, Long[].class);
        primitiveToBoxedTypeMap.put(float[].class, Float[].class);
        primitiveToBoxedTypeMap.put(double[].class, Double[].class);
    }

    /**
     * Converts primitive types to their boxed version.
     * <p>
     * Used to avoid needing to double-check everything
     *
     * @param clazz the class to convert
     * @return the boxed class type of the primitive one, {@code clazz} otherwise
     */
    public static Class getObjectClass(Class clazz) {
        return primitiveToBoxedTypeMap.getOrDefault(clazz, clazz);
    }

    public static String getObjectClassString(String clazz) {
        if (clazz.equals("boolean"))
            return "java.lang.Boolean";
        if (clazz.equals("byte"))
            return "java.lang.Byte";
        if (clazz.equals("char"))
            return "java.lang.Character";
        if (clazz.equals("short"))
            return "java.lang.Short";
        if (clazz.equals("int"))
            return "java.lang.Integer";
        if (clazz.equals("long"))
            return "java.lang.Long";
        if (clazz.equals("float"))
            return "java.lang.Float";
        if (clazz.equals("double"))
            return "java.lang.Double";
        return clazz;
    }

}
