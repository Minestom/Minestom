package net.minestom.server.codec;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.function.Function;

/**
 * Results are used in {@link Encoder} and {@link Decoder} to primarily function as a way of passing back exceptions as values.
 * <br>
 * They have two states {@link Ok} and {@link Error}, you can use pattern matching to extract the values
 * or use some of the helper methods provided like {@link #orElseThrow()} or {@link #mapResult(Function)}.
 * <br>
 * To construct simply just do {@code new Result.Ok<>(value) } and {@code new Result.Error<>("Error message!") }
 * <br>
 * You should not rely on the identity of results as they are value candidates.
 *
 * @param <T> the type, can be nullable.
 */
public sealed interface Result<T extends @UnknownNullability Object> {

    /**
     * Represents the {@link Result} was successful.
     *
     * @param value the value of {@link T}
     * @param <T>   the value type
     */
    record Ok<T extends @UnknownNullability Object>(T value) implements Result<T> {
    }

    /**
     * Represents the {@link Result} was a failure.
     *
     * @param message the message
     * @param <T>     the type
     */
    record Error<T>(String message) implements Result<T> {
        public Error {
            Objects.requireNonNull(message, "Message cannot be null");
        }
    }

    /**
     * Map the {@link Ok} result into the mapper function that creates a new result.
     * Otherwise, returns the error.
     *
     * @param mapper the new result
     * @param <S>    the type of the result.
     * @return the new result or the error.
     */
    @Contract(pure = true)
    default <S extends @UnknownNullability Object> Result<S> map(Function<T, Result<S>> mapper) {
        return this instanceof Ok<T>(T value) ? mapper.apply(value) : cast();
    }

    /**
     * Maps the {@link Ok} result to the mapper function and creates a new {@link Ok} result
     * Otherwise, returns the error.
     * <br>
     * Similar to {@link #map(Function)} but instead constructs the result instead.
     *
     * @param mapper the new result
     * @param <S>    the type of the result.
     * @return the new result or the error.
     */
    @Contract(pure = true)
    default <S extends @UnknownNullability Object> Result<S> mapResult(Function<T, S> mapper) {
        return this instanceof Ok<T>(T value) ? new Ok<>(mapper.apply(value)) : cast();
    }

    /**
     * Maps the {@link Error} result to the mapper function and creates a new {@link Error} result
     * Otherwise, returns {@link Ok}.
     * <br>
     * Similar to {@link #map(Function)} but instead constructs the result instead.
     *
     * @param mapper the new result
     * @return the new result or the error.
     */
    @Contract(pure = true)
    default Result<T> mapError(Function<String, String> mapper) {
        return this instanceof Error<?>(String message) ? new Error<>(mapper.apply(message)) : this;
    }

    /**
     * If the resultant is not {@link Ok}, returns the other value
     *
     * @param other value to be returned
     * @return the resultant
     */
    @Contract(pure = true)
    default @UnknownNullability T orElse(@UnknownNullability T other) {
        return this instanceof Ok<T>(T value)
                ? value : other;
    }

    /**
     * Attempts to get the value inside {@link Ok} or throws.
     *
     * @return the value
     * @throws IllegalStateException if this instance of {@link Error}
     */
    @Contract(pure = true)
    default T orElseThrow() {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Error<?>(String errorMessage) -> throw new IllegalArgumentException(errorMessage);
        };
    }

    /**
     * Attempts to get the value inside {@link Ok} or throws.
     *
     * @param message the message prefix
     * @return the value
     * @throws IllegalStateException if this instance of {@link Error}
     */
    @Contract(pure = true)
    default T orElseThrow(String message) {
        return switch (this) {
            case Ok<T>(T value) -> value;
            case Error<?>(String errorMessage) -> throw new IllegalArgumentException(
                    String.format("%s: %s", message, errorMessage)
            );
        };
    }

    /**
     * Casts the error to any type if present.
     * <br>
     * Useful to return the error if it is not the correct type.
     *
     * @param <S> the new result type
     * @return the error
     * @throws ClassCastException if the result is not {@link Error}
     */
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    default <S> Result.Error<S> cast() {
        if (!(this instanceof Result.Error<?>))
            throw new ClassCastException("Cannot cast a Result.Ok to a Result.Error");
        return (Result.Error<S>) this;
    }

}
