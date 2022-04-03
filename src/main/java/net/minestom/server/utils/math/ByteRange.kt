package net.minestom.server.utils.math

class ByteRange : Range<Byte> {
    constructor(minimum: Byte, maximum: Byte) : super(minimum, maximum) {}
    constructor(value: Byte) : super(value) {}

    /**
     * {@inheritDoc}
     */
    override fun isInRange(value: Byte): Boolean {
        return value >= minimum && value <= maximum
    }
}