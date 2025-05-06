package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBufferTemplate;

public interface Bounds<N extends Number> {

    N min();

    N max();

    static <N extends Number, R extends Bounds<N>> Codec<R> createCodec(Codec<N> codec, NetworkBufferTemplate.F2<N, N, R> ctor) {
        Codec<R> twoSided = StructCodec.struct(
                "min", codec, Bounds::min,
                "max", codec, Bounds::max,
                ctor
        );
        var oneSided = codec.transform(number -> ctor.apply(number, number), Bounds::min);

        // Bounds can be encoded as one number (an exact match) or two numbers (a range of values)
        return oneSided.orElse(twoSided);
    }

    record IntBounds(Integer min, Integer max) implements Bounds<Integer> {
        public static final Codec<IntBounds> CODEC = Bounds.createCodec(Codec.INT, IntBounds::new);
        public static final IntBounds ANY = new IntBounds(null, null);

        public boolean matches(int i) {
            return (this.min() == null || i >= this.min()) && (this.max() == null || i <= this.max());
        }
    }

    record DoubleBounds(Double min, Double max) implements Bounds<Double> {
        public static Codec<DoubleBounds> CODEC = Bounds.createCodec(Codec.DOUBLE, DoubleBounds::new);
        public static final DoubleBounds ANY = new DoubleBounds(null, null);

        public boolean matches(double d) {
            return (this.min() == null || d >= this.min()) && (this.max() == null || d <= this.max());
        }
    }
}
