package net.minestom.server.utils;

import java.util.function.Function;

public record Either<L, R>(boolean isLeft, L left, R right) {
    public static <T, U> Either<T, U> left(T left) {
        return new Either<>(true, left, null);
    }
    public static <T, U> Either<U, T> right(T right) {
        return new Either<>(false, null, right);
    }

    public <T> T map(Function<L, T> leftMapper, Function<R, T> rightMapper) {
        if (isLeft) {
            return leftMapper.apply(left);
        } else {
            return rightMapper.apply(right);
        }
    }
}
