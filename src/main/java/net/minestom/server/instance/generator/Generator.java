package net.minestom.server.instance.generator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface Generator {
    void generate(@NotNull GenerationUnit unit);

    default void generateAll(@NotNull List<GenerationUnit> units) {
        units.forEach(this::generate);
    }
}
