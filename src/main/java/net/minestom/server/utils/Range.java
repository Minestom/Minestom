package net.minestom.server.utils;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the base for any data type that is numeric.
 *
 * @param <T> The type numeric of the range object.
 */
public sealed interface Range<T extends Number> {
    record Byte(@Nullable java.lang.Byte min, @Nullable java.lang.Byte max) implements Range<java.lang.Byte> {
        public Byte {
            if (min != null && max != null && min > max) throw new IllegalArgumentException("min must not exceed max");
        }

        public Byte(@Nullable java.lang.Byte value) {
            this(value, value);
        }

        public boolean inRange(byte value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Short(@Nullable java.lang.Short min, @Nullable java.lang.Short max) implements Range<java.lang.Short> {
        public Short {
            if (min != null && max != null && min > max) throw new IllegalArgumentException("min must not exceed max");
        }

        public Short(@Nullable java.lang.Short value) {
            this(value, value);
        }

        public boolean inRange(short value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Int(@Nullable Integer min, @Nullable Integer max) implements Range<Integer> {
        private static final Codec<Range.Int> STRUCT_CODEC = StructCodec.struct(
                "min", Codec.INT.optional(), Range.Int::min,
                "max", Codec.INT.optional(), Range.Int::max,
                Range.Int::new
        );
        public static final Codec<Range.Int> CODEC = Codec.Either(STRUCT_CODEC, Codec.INT).transform(
                either -> either.unify(range -> range, Range.Int::new),
                range -> range.min != null && range.min.equals(range.max) ? Either.right(range.min) : Either.left(range));

        public Int {
            if (min != null && max != null && min > max) throw new IllegalArgumentException("min must not exceed max");
        }

        public Int(@Nullable Integer value) {
            this(value, value);
        }

        public boolean inRange(int value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Long(@Nullable java.lang.Long min, @Nullable java.lang.Long max) implements Range<java.lang.Long> {
        public Long {
            if (min != null && max != null && min > max) throw new IllegalArgumentException("min must not exceed max");
        }

        public Long(@Nullable java.lang.Long value) {
            this(value, value);
        }

        public boolean inRange(long value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Float(@Nullable java.lang.Float min, @Nullable java.lang.Float max) implements Range<java.lang.Float> {
        public Float {
            if (min != null && max != null && min > max) throw new IllegalArgumentException("min must not exceed max");
        }

        public Float(@Nullable java.lang.Float value) {
            this(value, value);
        }

        public boolean inRange(float value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }

    record Double(@Nullable java.lang.Double min, @Nullable java.lang.Double max) implements Range<java.lang.Double> {
        private static final Codec<Range.Double> STRUCT_CODEC = StructCodec.struct(
                "min", Codec.DOUBLE.optional(), Range.Double::min,
                "max", Codec.DOUBLE.optional(), Range.Double::max,
                Range.Double::new
        );
        public static final Codec<Range.Double> CODEC = Codec.Either(STRUCT_CODEC, Codec.DOUBLE).transform(
                either -> either.unify(range -> range, Range.Double::new),
                range -> range.min != null && range.min.equals(range.max) ? Either.right(range.min) : Either.left(range));

        public Double {
            if (min != null && max != null && min > max) throw new IllegalArgumentException("min must not exceed max");
        }

        public Double(@Nullable java.lang.Double value) {
            this(value, value);
        }

        public boolean inRange(double value) {
            return (min == null || value >= min) && (max == null || value <= max);
        }
    }
}
