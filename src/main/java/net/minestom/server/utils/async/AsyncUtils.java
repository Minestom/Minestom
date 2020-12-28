package net.minestom.server.utils.async;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class AsyncUtils {

    public static void runAsync(@NotNull Runnable runnable) {
        CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
