package net.minestom.codegen.util;

public final class NameUtil {
    private NameUtil() {

    }

    public static String convertSnakeCaseToCamelCase(String snakeCase) {
        StringBuilder sb = new StringBuilder(snakeCase);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '_') {
                sb.deleteCharAt(i);
                sb.replace(i, i + 1, String.valueOf(Character.toUpperCase(sb.charAt(i))));
            }
        }

        // Capitalize first letter.
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }
}
