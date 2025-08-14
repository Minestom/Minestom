package net.minestom.server.codec;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

@ApiStatus.Experimental
public sealed interface Result<T extends @UnknownNullability Object> {

    record Ok<T extends @UnknownNullability Object>(T value) implements Result<T> {
    }

    record Error<T>(String message) implements Result<T> {
    }

    @Contract(pure = true)
    default <S> Result<S> map(Function<T, Result<S>> mapper) {
        return this instanceof Ok<T>(T value) ? mapper.apply(value) : cast();
    }

    @Contract(pure = true)
    default <S> Result<S> mapResult(Function<T, S> mapper) {
        return this instanceof Ok<T>(T value) ? new Ok<>(mapper.apply(value)) : cast();
    }
    @Contract(pure = true)
    default Result<T> mapError(Function<String, String> mapper) {
        return this instanceof Error<?>(String message) ? new Error<>(mapper.apply(message)) : this;
    }

    @Contract(pure = true)
    default @UnknownNullability T orElse(@Nullable T other) {
        return this instanceof Ok<T>(T value)
                ? value : other;
    }

    @Contract(pure = true)
    default T orElseThrow() {
        return orElseThrow(null);
    }

    @Contract(pure = true)
    default T orElseThrow(@Nullable String message) {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Error<?>(String errorMessage) -> throw new IllegalArgumentException(
                    message != null ? String.format("%s: %s", message, errorMessage) : errorMessage
            );
        };
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    default <S> Result<S> cast() {
        if (!(this instanceof Result.Error<?>))
            throw new ClassCastException("Cannot cast a Result.Ok to a Result.Error");
        return (Result.Error<S>) this;
    }

}
