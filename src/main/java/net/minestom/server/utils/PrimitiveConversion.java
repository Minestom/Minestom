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
