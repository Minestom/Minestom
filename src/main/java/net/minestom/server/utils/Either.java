package net.minestom.server.utils;

import java.util.Objects;
import java.util.function.Function;

public sealed interface Either<L, R> {

    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
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

    default <T> T unify(Function<L, T> leftMapper, Function<R, T> rightMapper) {
        return switch (this) {
            case Left(L value) -> leftMapper.apply(value);
            case Right(R value) -> rightMapper.apply(value);
        };
    }

}
