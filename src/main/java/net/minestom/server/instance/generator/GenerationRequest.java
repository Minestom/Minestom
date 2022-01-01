package net.minestom.server.instance.generator;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface GenerationRequest {
    void returnAsync(@NotNull CompletableFuture<?> future);

    @NotNull GenerationUnit unit();
}
