package net.minestom.server.utils.math;

public class ByteRange extends Range<Byte> {

    public ByteRange(Byte minimum, Byte maximum) {
        super(minimum, maximum);
    }

    public ByteRange(Byte value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInRange(Byte value) {
        return value >= this.getMinimum() && value <= this.getMaximum();
    }
}
