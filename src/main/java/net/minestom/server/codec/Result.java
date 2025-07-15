package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@ApiStatus.Experimental
public sealed interface Result<T> {

    record Ok<T>(T value) implements Result<T> {
    }

    record Error<T>(String message) implements Result<T> {
    }

    default <S> Result<S> map(Function<T, Result<S>> mapper) {
        return this instanceof Ok<T>(T value) ? mapper.apply(value) : cast();
    }

    default <S> Result<S> mapResult(Function<T, S> mapper) {
        return this instanceof Ok<T>(T value) ? new Ok<>(mapper.apply(value)) : cast();
    }

    default Result<T> mapError(Function<String, String> mapper) {
        return this instanceof Error<T>(String message) ? new Error<>(mapper.apply(message)) : this;
    }

    default T orElse(T other) {
        return this instanceof Ok<T>(T value)
                ? value : other;
    }

    default T orElseThrow() {
        return orElseThrow(null);
    }

    default T orElseThrow(@Nullable String message) {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Error<T>(String errorMessage) -> throw new IllegalArgumentException(
                    message != null ? String.format("%s: %s", message, errorMessage) : errorMessage
            );
        };
    }

    default <S> Result<S> cast() {
        if (!(this instanceof Result.Error<T>))
            throw new ClassCastException("Cannot cast a Result.Ok to a Result.Error");
        //noinspection unchecked
        return (Result.Error<S>) this;
    }

}
