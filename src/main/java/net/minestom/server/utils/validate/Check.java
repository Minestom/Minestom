package net.minestom.server.utils.validate;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Convenient class to check for common exceptions.
 */
public final class Check {

    private Check() {

    }

    @Contract("null, _ -> fail")
    public static void notNull(@Nullable Object object, String reason) {
        if (Objects.isNull(object)) {
            throw new NullPointerException(reason);
        }
    }

    @Contract("null, _, _ -> fail")
    public static void notNull(@Nullable Object object, String reason, Object... arguments) {
        if (Objects.isNull(object)) {
            throw new NullPointerException(MessageFormat.format(reason, arguments));
        }
    }

    @Contract("true, _ -> fail")
    public static void argCondition(boolean condition, String reason) {
        if (condition) {
            throw new IllegalArgumentException(reason);
        }
    }

    @Contract("true, _, _ -> fail")
    public static void argCondition(boolean condition, String reason, Object... arguments) {
        if (condition) {
            throw new IllegalArgumentException(MessageFormat.format(reason, arguments));
        }
    }

    @Contract("_ -> fail")
    public static void fail(String reason) {
        throw new IllegalArgumentException(reason);
    }

    @Contract("_, _ -> fail")
    public static void fail(String reason, Object... arguments) {
        throw new IllegalArgumentException(MessageFormat.format(reason, arguments));
    }

    @Contract("true, _ -> fail")
    public static void stateCondition(boolean condition, String reason) {
        if (condition) {
            throw new IllegalStateException(reason);
        }
    }

    @Contract("true, _, _ -> fail")
    public static void stateCondition(boolean condition, String reason, Object... arguments) {
        if (condition) {
            throw new IllegalStateException(MessageFormat.format(reason, arguments));
        }
    }

    @Contract("false, _ -> fail")
    public static void isTrue(boolean condition, String reason) {
        if (!condition) {
            throw new IllegalStateException(reason);
        }
    }

    @Contract("false, _, _ -> fail")
    public static void isTrue(boolean condition, String reason, Object... arguments) {
        if (!condition) {
            throw new IllegalStateException(MessageFormat.format(reason, arguments));
        }
    }
}
