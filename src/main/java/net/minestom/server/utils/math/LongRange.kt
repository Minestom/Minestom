package net.minestom.server.utils.math

class LongRange : Range<Long> {
    constructor(minimum: Long, maximum: Long) : super(minimum, maximum) {}
    constructor(value: Long) : super(value) {}

    /**
     * {@inheritDoc}
     */
    override fun isInRange(value: Long): Boolean {
        return value >= minimum && value <= maximum
    }
}