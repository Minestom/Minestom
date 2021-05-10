package net.minestom.server.utils.math;

public class LongRange extends Range<Long> {

    public LongRange(Long minimum, Long maximum) {
        super(minimum, maximum);
    }

    public LongRange(Long value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInRange(Long value) {
        return value >= this.getMinimum() && value <= this.getMaximum();
    }
}
