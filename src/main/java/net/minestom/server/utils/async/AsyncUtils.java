package net.minestom.server.utils.async;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class AsyncUtils {
    public static final CompletableFuture<Void> NULL_FUTURE = CompletableFuture.completedFuture(null);

    public static @NotNull CompletableFuture<Void> runAsync(@NotNull Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
    }
}
