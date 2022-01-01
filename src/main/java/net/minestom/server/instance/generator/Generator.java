package net.minestom.server.instance.generator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface Generator {
    void generate(@NotNull GenerationRequest request);

    default void generateAll(@NotNull List<GenerationRequest> requests) {
        requests.forEach(this::generate);
    }
}
