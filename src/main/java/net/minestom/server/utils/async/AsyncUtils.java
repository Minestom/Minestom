package net.minestom.server.utils.async;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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

    public static <T> CompletableFuture<List<T>> allOf(CompletableFuture<T>[] futures) {
        return allOf(List.of(futures));
    }

    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futures) {
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            }
        });
        return future;
    }
}
