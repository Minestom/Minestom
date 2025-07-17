package net.minestom.server.utils;

/**
 * Represents the base for any data type that is numeric.
 *
 * @param <T> The type numeric of the range object.
 */
public sealed interface Range<T extends Number> {
    record Byte(byte min, byte max) implements Range<java.lang.Byte> {
        public Byte(byte value) {
            this(value, value);
        }

        public boolean inRange(byte value) {
            return value >= min && value <= max;
        }
    }

    record Short(short min, short max) implements Range<java.lang.Short> {
        public Short(short value) {
            this(value, value);
        }

        public boolean inRange(short value) {
            return value >= min && value <= max;
        }
    }

    record Int(int min, int max) implements Range<java.lang.Integer> {
        public Int(int value) {
            this(value, value);
        }

        public boolean inRange(int value) {
            return value >= min && value <= max;
        }
    }

    record Long(long min, long max) implements Range<java.lang.Long> {
        public Long(long value) {
            this(value, value);
        }

        public boolean inRange(long value) {
            return value >= min && value <= max;
        }
    }

    record Float(float min, float max) implements Range<java.lang.Float> {
        public Float(float value) {
            this(value, value);
        }

        public boolean inRange(float value) {
            return value >= min && value <= max;
        }
    }

    record Double(double min, double max) implements Range<java.lang.Double> {
        public Double(double value) {
            this(value, value);
        }

        public boolean inRange(double value) {
            return value >= min && value <= max;
        }
    }
}
