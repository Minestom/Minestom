package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PropertyUtils {
    private PropertyUtils() {}

    public static boolean getBoolean(String name, boolean defaultValue) {
        boolean result = defaultValue;
        try {
            final String value = System.getProperty(name);
            if (value != null) result = Boolean.parseBoolean(value);
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }
        return result;
    }
}
