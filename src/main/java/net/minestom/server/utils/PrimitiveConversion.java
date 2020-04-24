package net.minestom.server.utils;

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

    public static String getObjectClassString(String clazz) {
        if (clazz == "boolean")
            return "java.lang.Boolean";
        if (clazz == "byte")
            return "java.lang.Byte";
        if (clazz == "char")
            return "java.lang.Character";
        if (clazz == "short")
            return "java.lang.Short";
        if (clazz == "int")
            return "java.lang.Integer";
        if (clazz == "long")
            return "java.lang.Long";
        if (clazz == "float")
            return "java.lang.Float";
        if (clazz == "double")
            return "java.lang.Double";
        return clazz;
    }

}
