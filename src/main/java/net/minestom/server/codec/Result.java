package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@ApiStatus.Experimental
public sealed interface Result<T> {

    record Ok<T>(T value) implements Result<T> {
    }

    record Error<T>(@NotNull String message) implements Result<T> {
    }

    default <S> Result<S> map(Function<T, S> mapper) {
        return this instanceof Ok<T>(T value) ? new Ok<>(mapper.apply(value)) : cast();
    }

    default Result<T> mapError(Function<String, String> mapper) {
        return this instanceof Error<T>(String message) ? new Error<>(mapper.apply(message)) : this;
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
