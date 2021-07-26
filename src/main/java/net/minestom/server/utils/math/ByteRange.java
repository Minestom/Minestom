package net.minestom.server.utils.math;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ByteRange {

    private final byte minimum;
    private final byte maximum;

    /**
     * Constructs a new {@link ByteRange} with a {@code minimum} and a {@code maximum} value.
     *
     * @param minimum The minimum of the range.
     * @param maximum The maximum of the range.
     */
    public ByteRange(byte minimum, byte maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Constructs a new {@link ByteRange} with the {@code value}.
     *
     * @param value The value of the range.
     */
    public ByteRange(byte value) {
        this(value, value);
    }

    /**
     * Retrieves the minimum value of the range.
     *
     * @return The range's minimum value.
     */
    public byte minimum() {
        return this.minimum;
    }

    /**
     * Retrieves the maximum value of the range.
     *
     * @return The range's maximum value.
     */
    public byte maximum() {
        return this.maximum;
    }

    /**
     * Whether the given {@code value} is in range of the minimum and the maximum.
     *
     * @param value The value to be checked.
     * @return {@code true} if the value in the range of {@code minimum} and {@code maximum},
     *     otherwise {@code false}.
     */
    public boolean isInRange(byte value) {
        return value >= this.minimum && value <= this.maximum;
    }

    /**
     * Gets a random number between {@code minimum} and {@code maximum}
     *
     * @param random The ThreadLocalRandom to use
     *
     * @return A random number between {@code minimum} and {@code maximum}
     */
    public byte random(@NotNull Random random) {
        return (byte) (minimum + (random.nextDouble() * (maximum - minimum)));
    }
}