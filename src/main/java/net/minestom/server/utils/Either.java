package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;

public sealed interface Either<L, R> {

    static <L, R> @NotNull Either<L, R> left(@NotNull L value) {
        return new Left<>(value);
    }

    static <L, R> @NotNull Either<L, R> right(@NotNull R value) {
        return new Right<>(value);
    }

    record Left<L, R>(@NotNull L value) implements Either<L, R> {
    }

    record Right<L, R>(@NotNull R value) implements Either<L, R> {
    }

}
