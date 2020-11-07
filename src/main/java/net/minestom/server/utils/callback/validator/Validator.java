package net.minestom.server.utils.callback.validator;

import org.jetbrains.annotations.NotNull;

/**
 * Interface used when a value needs to be validated dynamically.
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Gets if a value is valid based on a condition.
     *
     * @param value the value to check
     * @return true if the value is valid, false otherwise
     */
    boolean isValid(@NotNull T value);

}
