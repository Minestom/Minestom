package net.minestom.server.utils.math;

public class ShortRange extends Range<Short> {

  public ShortRange(Short minimum, Short maximum) {
    super(minimum, maximum);
  }

  public ShortRange(Short value) {
    super(value);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isInRange(Short value) {
    return value >= this.getMinimum() && value <= this.getMaximum();
  }
}
