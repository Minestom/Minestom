package net.minestom.server.utils.math;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * A range from a minimum value to a maximum value.
 */
public class IntRange {

    private final int minimum;
    private final int maximum;

    /**
     * Constructs a new {@link IntRange} with a {@code minimum} and a {@code maximum} value.
     *
     * @param minimum The minimum of the range.
     * @param maximum The maximum of the range.
     */
    public IntRange(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Constructs a new {@link IntRange} with the {@code value}.
     *
     * @param value The value of the range.
     */
    public IntRange(int value) {
        this(value, value);
    }

    /**
     * Retrieves the minimum value of the range.
     *
     * @return The range's minimum value.
     */
    public int minimum() {
        return this.minimum;
    }

    /**
     * Retrieves the maximum value of the range.
     *
     * @return The range's maximum value.
     */
    public int maximum() {
        return this.maximum;
    }

    /**
     * Whether the given {@code value} is in range of the minimum and the maximum.
     *
     * @param value The value to be checked.
     * @return {@code true} if the value in the range of {@code minimum} and {@code maximum},
     *     otherwise {@code false}.
     */
    public boolean isInRange(int value) {
        return value >= this.minimum && value <= this.maximum;
    }

    /**
     * Gets a random number between {@code minimum} and {@code maximum}
     *
     * @param random The ThreadLocalRandom to use
     *
     * @return A random number between {@code minimum} and {@code maximum}
     */
    public int random(@NotNull Random random) {
        return minimum + (int) (random.nextDouble() * (maximum - minimum));
    }
}