package net.minestom.server.utils;

import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.function.Function;

public sealed interface Either<L, R> {

    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    static <V> V identity(Either<? extends V, ? extends V> either) {
        return either.unify(Function.identity(), Function.identity());
    }

    record Left<L, R>(L value) implements Either<L, R> {
        public Left {
            Objects.requireNonNull(value, "Left value must not be null");
        }
    }

    record Right<L, R>(R value) implements Either<L, R> {
        public Right {
            Objects.requireNonNull(value, "Right value must not be null");
        }
    }

    default <T extends @UnknownNullability Object> T unify(Function<? super L, ? extends T> leftMapper, Function<? super R, ? extends T> rightMapper) {
        return switch (this) {
            case Left(L value) -> leftMapper.apply(value);
            case Right(R value) -> rightMapper.apply(value);
        };
    }

}
