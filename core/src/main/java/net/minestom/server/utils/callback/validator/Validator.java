package net.minestom.server.utils.callback.validator;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Interface used when a value needs to be validated dynamically.
 */
@FunctionalInterface
public interface Validator<T> extends Predicate<T> {

    /**
     * Gets if a value is valid based on a condition.
     *
     * @param value the value to check
     * @return true if the value is valid, false otherwise
     */
    boolean isValid(@NotNull T value);

    @Override
    default boolean test(T t) {
        if (t == null) {
            return false;
        } else {
            return this.isValid(t);
        }
    }
}
