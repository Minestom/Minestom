package fr.themode.minestom.utils;

public class PrimitiveConversion {

    public static Class getObjectClass(Class clazz) {
        if (clazz == boolean.class)
            return Boolean.class;
        if (clazz == byte.class)
            return Byte.class;
        if (clazz == char.class)
            return Character.class;
        if (clazz == short.class)
            return Short.class;
        if (clazz == int.class)
            return Integer.class;
        if (clazz == long.class)
            return Long.class;
        if (clazz == float.class)
            return Float.class;
        if (clazz == double.class)
            return Double.class;
        return clazz;
    }

}
