package net.minestom.server.utils.validate;

import net.minestom.server.MinecraftServer;
import net.minestom.server.acquirable.Acquirable;
import net.minestom.server.thread.TickThread;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Convenient class to check for common exceptions.
 */
public final class Check {

    private Check() {

    }

    public static void ensureStrictMode(Acquirable<?> acquirable) {
        if (!MinecraftServer.STRICT_MODE) return;
        final TickThread thread = acquirable.getHandler().getTickThread();
        if (Thread.currentThread() == thread) return;
        if (thread != null && thread.lock().isHeldByCurrentThread()) return;
        // TODO if thread is null but the global lock is acquired
        throw new IllegalStateException("Entity is accessed from an unauthorized thread!");
    }

    @Contract("null, _ -> fail")
    public static void notNull(@Nullable Object object, @NotNull String reason) {
        if (Objects.isNull(object)) {
            throw new NullPointerException(reason);
        }
    }

    @Contract("null, _, _ -> fail")
    public static void notNull(@Nullable Object object, @NotNull String reason, Object... arguments) {
        if (Objects.isNull(object)) {
            throw new NullPointerException(MessageFormat.format(reason, arguments));
        }
    }

    @Contract("true, _ -> fail")
    public static void argCondition(boolean condition, @NotNull String reason) {
        if (condition) {
            throw new IllegalArgumentException(reason);
        }
    }

    @Contract("true, _, _ -> fail")
    public static void argCondition(boolean condition, @NotNull String reason, Object... arguments) {
        if (condition) {
            throw new IllegalArgumentException(MessageFormat.format(reason, arguments));
        }
    }

    @Contract("_ -> fail")
    public static void fail(@NotNull String reason) {
        throw new IllegalArgumentException(reason);
    }

    @Contract("true, _ -> fail")
    public static void stateCondition(boolean condition, @NotNull String reason) {
        if (condition) {
            throw new IllegalStateException(reason);
        }
    }

    @Contract("true, _, _ -> fail")
    public static void stateCondition(boolean condition, @NotNull String reason, Object... arguments) {
        if (condition) {
            throw new IllegalStateException(MessageFormat.format(reason, arguments));
        }
    }

}
