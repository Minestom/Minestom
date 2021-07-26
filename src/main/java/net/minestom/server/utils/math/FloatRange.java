package net.minestom.server.utils.math;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * A range from a minimum value to a maximum value.
 */
public class FloatRange {

    private final float minimum;
    private final float maximum;

    /**
     * Constructs a new {@link FloatRange} with a {@code minimum} and a {@code maximum} value.
     *
     * @param minimum The minimum of the range.
     * @param maximum The maximum of the range.
     */
    public FloatRange(float minimum, float maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Constructs a new {@link FloatRange} with the {@code value}.
     *
     * @param value The value of the range.
     */
    public FloatRange(float value) {
        this(value, value);
    }

    /**
     * Retrieves the minimum value of the range.
     *
     * @return The range's minimum value.
     */
    public float minimum() {
        return this.minimum;
    }

    /**
     * Retrieves the maximum value of the range.
     *
     * @return The range's maximum value.
     */
    public float maximum() {
        return this.maximum;
    }

    /**
     * Whether the given {@code value} is in range of the minimum and the maximum.
     *
     * @param value The value to be checked.
     * @return {@code true} if the value in the range of {@code minimum} and {@code maximum},
     *     otherwise {@code false}.
     */
    public boolean isInRange(float value) {
        return value >= this.minimum && value <= this.maximum;
    }

    /**
     * Gets a random number between {@code minimum} and {@code maximum}
     *
     * @param random The ThreadLocalRandom to use
     *
     * @return A random number between {@code minimum} and {@code maximum}
     */
    public float random(@NotNull Random random) {
        return minimum + (float) (random.nextDouble() * (maximum - minimum));
    }
}