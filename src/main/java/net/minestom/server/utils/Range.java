package net.minestom.server.utils;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the base for any data type that is numeric.
 *
 * @param <T> The type numeric of the range object.
 */
public sealed interface Range<T extends Number> {
    record Byte(@Nullable java.lang.Byte min, @Nullable java.lang.Byte max) implements Range<java.lang.Byte> {
        public Byte(@Nullable java.lang.Byte value) {
            this(value, value);
        }

        public boolean inRange(byte value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Short(@Nullable java.lang.Short min, @Nullable java.lang.Short max) implements Range<java.lang.Short> {
        public Short(@Nullable java.lang.Short value) {
            this(value, value);
        }

        public boolean inRange(short value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Int(@Nullable java.lang.Integer min, @Nullable java.lang.Integer max) implements Range<java.lang.Integer> {
        public Int(@Nullable java.lang.Integer value) {
            this(value, value);
        }

        public boolean inRange(int value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Long(@Nullable java.lang.Long min, @Nullable java.lang.Long max) implements Range<java.lang.Long> {
        public Long(@Nullable java.lang.Long value) {
            this(value, value);
        }

        public boolean inRange(long value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Float(@Nullable java.lang.Float min, @Nullable java.lang.Float max) implements Range<java.lang.Float> {
        public Float(@Nullable java.lang.Float value) {
            this(value, value);
        }

        public boolean inRange(float value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Double(@Nullable java.lang.Double min, @Nullable java.lang.Double max) implements Range<java.lang.Double> {
        public Double(@Nullable java.lang.Double value) {
            this(value, value);
        }

        public boolean inRange(double value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }
}
