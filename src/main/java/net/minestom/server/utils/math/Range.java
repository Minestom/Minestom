package net.minestom.server.utils.math;

/**
 * Represents the base for any data type that is numeric.
 *
 * @param <T> The type numeric of the range object.
 */
public abstract class Range<T> {

  private T minimum;
  private T maximum;

  /**
   * Constructs a new {@link Range} with a {@code minimum} and a {@code maximum} value.
   *
   * @param minimum The minimum of the range.
   * @param maximum The maximum of the range.
   */
  public Range(T minimum, T maximum) {
    this.minimum = minimum;
    this.maximum = maximum;
  }

  /**
   * Constructs a new {@link Range} with the {@code value}.
   *
   * @param value The value of the range.
   */
  public Range(T value) {
    this(value, value);
  }

  /**
   * Retrieves the minimum value of the range.
   *
   * @return The range's minimum value.
   */
  public T getMinimum() {
    return this.minimum;
  }

  /**
   * Changes the minimum value of the range.
   *
   * @param minimum The new minimum value.
   */
  public void setMinimum(T minimum) {
    this.minimum = minimum;
  }

  /**
   * Retrieves the maximum value of the range.
   *
   * @return The range's maximum value.
   */
  public T getMaximum() {
    return this.maximum;
  }

  /**
   * Changes the maximum value of the range.
   *
   * @param maximum The new maximum value.
   */
  public void setMaximum(T maximum) {
    this.maximum = maximum;
  }

  /**
   * Whether the given {@code value} is in range of the minimum and the maximum.
   *
   * @param value The value to be checked.
   * @return {@code true} if the value in the range of {@code minimum} and {@code maximum},
   *     otherwise {@code false}.
   */
  public abstract boolean isInRange(T value);
}
