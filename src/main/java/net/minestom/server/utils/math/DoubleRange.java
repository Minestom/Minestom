package net.minestom.server.utils.math;

public class DoubleRange extends Range<Double> {

    public DoubleRange(Double minimum, Double maximum) {
        super(minimum, maximum);
    }

    public DoubleRange(Double value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInRange(Double value) {
        return value >= this.getMinimum() && value <= this.getMaximum();
    }
}
