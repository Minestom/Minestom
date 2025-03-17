package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public sealed interface Result<T> {

    record Ok<T>(T value) implements Result<T> {
    }

    record Error<T>(@NotNull String message) implements Result<T> {
    }

    default T orElse(T other) {
        return this instanceof Ok<T>(T value)
                ? value : other;
    }

    default <S> Result<S> cast() {
        if (!(this instanceof Result.Error<T>))
            throw new ClassCastException("Cannot cast a Result.Ok to a Result.Error");
        //noinspection unchecked
        return (Result.Error<S>) this;
    }

}
