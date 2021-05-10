package net.minestom.server.utils.math;

public class IntRange extends Range<Integer> {

    public IntRange(Integer minimum, Integer maximum) {
        super(minimum, maximum);
    }

    public IntRange(Integer value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInRange(Integer value) {
        return value >= this.getMinimum() && value <= this.getMaximum();
    }
}
