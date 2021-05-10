package net.minestom.server.utils.math;

public class FloatRange extends Range<Float> {

  public FloatRange(Float minimum, Float maximum) {
    super(minimum, maximum);
  }

  public FloatRange(Float value) {
    super(value);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isInRange(Float value) {
    return value >= this.getMinimum() && value <= this.getMaximum();

  }
}
