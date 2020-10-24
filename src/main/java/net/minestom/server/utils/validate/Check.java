package net.minestom.server.utils.validate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Check {
    private Check() {

    }

    public static void notNull(@Nullable Object object, @NotNull String reason) {
        if (Objects.isNull(object)) {
            throw new NullPointerException(reason);
        }
    }

    public static void argCondition(boolean condition, @NotNull String reason) {
        if (condition) {
            throw new IllegalArgumentException(reason);
        }
    }

    public static void stateCondition(boolean condition, @NotNull String reason) {
        if (condition) {
            throw new IllegalStateException(reason);
        }
    }

}
