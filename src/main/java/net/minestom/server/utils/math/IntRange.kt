package net.minestom.server.utils.math

class IntRange : Range<Int> {
    constructor(minimum: Int, maximum: Int) : super(minimum, maximum) {}
    constructor(value: Int) : super(value) {}

    /**
     * {@inheritDoc}
     */
    override fun isInRange(value: Int): Boolean {
        return value >= minimum && value <= maximum
    }
}