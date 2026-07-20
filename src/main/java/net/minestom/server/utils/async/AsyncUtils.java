package net.minestom.server.utils.async;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public final class AsyncUtils {
    public static final CompletableFuture<Void> VOID_FUTURE = CompletableFuture.completedFuture(null);

    public static <T extends @Nullable Object> CompletableFuture<T> empty() {
        //noinspection unchecked
        return (CompletableFuture<T>) VOID_FUTURE;
    }
}
