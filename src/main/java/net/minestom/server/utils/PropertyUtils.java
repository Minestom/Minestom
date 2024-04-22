package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Contract("_, null -> null; _, !null -> !null")
    public static String getString(@NotNull String name, @Nullable String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    public static Float getFloat(String name, Float defaultValue) {
        Float result = defaultValue;
        try {
            final String value = System.getProperty(name);
            if (value != null) result = Float.parseFloat(value);
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }
        return result;
    }
}
