package net.minestom.server.utils.math

class DoubleRange : Range<Double> {
    constructor(minimum: Double, maximum: Double) : super(minimum, maximum) {}
    constructor(value: Double) : super(value) {}

    /**
     * {@inheritDoc}
     */
    override fun isInRange(value: Double): Boolean {
        return value >= minimum && value <= maximum
    }
}