package net.minestom.server.utils.math

class FloatRange : Range<Float> {
    constructor(minimum: Float, maximum: Float) : super(minimum, maximum) {}
    constructor(value: Float) : super(value) {}

    /** {@inheritDoc}  */
    override fun isInRange(value: Float): Boolean {
        return value >= minimum && value <= maximum
    }
}