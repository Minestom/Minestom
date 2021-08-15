package net.minestom.server.utils.async;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public final class AsyncUtils {
    public static final CompletableFuture<Void> VOID_FUTURE = CompletableFuture.completedFuture(null);

    public static <T> CompletableFuture<T> empty() {
        //noinspection unchecked
        return (CompletableFuture<T>) VOID_FUTURE;
    }

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
