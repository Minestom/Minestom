package net.minestom.server.utils.math

class ShortRange : Range<Short> {
    constructor(minimum: Short, maximum: Short) : super(minimum, maximum) {}
    constructor(value: Short) : super(value) {}

    /** {@inheritDoc}  */
    override fun isInRange(value: Short): Boolean {
        return value >= minimum && value <= maximum
    }
}