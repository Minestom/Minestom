package net.minestom.server.utils.math

/**
 * Represents the base for any data type that is numeric.
 *
 * @param <T> The type numeric of the range object.
</T> */
abstract class Range<T>
/**
 * Constructs a new [Range] with a `minimum` and a `maximum` value.
 *
 * @param minimum The minimum of the range.
 * @param maximum The maximum of the range.
 */(
    /**
     * Changes the minimum value of the range.
     *
     * @param minimum The new minimum value.
     */
    var minimum: T,
    /**
     * Changes the maximum value of the range.
     *
     * @param maximum The new maximum value.
     */
    var maximum: T
) {
    /**
     * Retrieves the minimum value of the range.
     *
     * @return The range's minimum value.
     */
    /**
     * Retrieves the maximum value of the range.
     *
     * @return The range's maximum value.
     */

    /**
     * Constructs a new [Range] with the `value`.
     *
     * @param value The value of the range.
     */
    constructor(value: T) : this(value, value) {}

    /**
     * Whether the given `value` is in range of the minimum and the maximum.
     *
     * @param value The value to be checked.
     * @return `true` if the value in the range of `minimum` and `maximum`,
     * otherwise `false`.
     */
    abstract fun isInRange(value: T): Boolean
}