package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

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

    static <N extends Number, R extends Bounds<N>> NetworkBuffer.Type<R> createNetworkType(NetworkBuffer.Type<N> type, BiFunction<N, N, R> ctor) {
        return new NetworkBuffer.Type<>() {
            private static final byte MIN_FLAG = 1;
            private static final byte MAX_FLAG = 2;

            @Override
            public void write(@NotNull NetworkBuffer buffer, R value) {
                NetworkBuffer.BYTE.write(buffer, (byte) ((value.min() != null ? MIN_FLAG : 0) | (value.max() != null ? MAX_FLAG : 0)));
                if (value.min() != null) {
                    type.write(buffer, value.min());
                }
                if (value.max() != null) {
                    type.write(buffer, value.max());
                }
            }

            @Override
            public R read(@NotNull NetworkBuffer buffer) {
                byte flags = NetworkBuffer.BYTE.read(buffer);
                N min = (flags & MIN_FLAG) != 0 ? type.read(buffer) : null;
                N max = (flags & MAX_FLAG) != 0 ? type.read(buffer) : null;
                return ctor.apply(min, max);
            }
        };
    }

    record IntBounds(Integer min, Integer max) implements Bounds<Integer> {
        public static final Codec<IntBounds> CODEC = Bounds.createCodec(Codec.INT, IntBounds::new);
        public static final NetworkBuffer.Type<IntBounds> NETWORK_TYPE = Bounds.createNetworkType(NetworkBuffer.VAR_INT, IntBounds::new);
        public static final IntBounds ANY = new IntBounds(null, null);

        public boolean matches(int i) {
            return (this.min() == null || i >= this.min()) && (this.max() == null || i <= this.max());
        }
    }

    record DoubleBounds(Double min, Double max) implements Bounds<Double> {
        public static Codec<DoubleBounds> CODEC = Bounds.createCodec(Codec.DOUBLE, DoubleBounds::new);
        public static NetworkBuffer.Type<DoubleBounds> NETWORK_TYPE = Bounds.createNetworkType(NetworkBuffer.DOUBLE, DoubleBounds::new);
        public static final DoubleBounds ANY = new DoubleBounds(null, null);

        public boolean matches(double d) {
            return (this.min() == null || d >= this.min()) && (this.max() == null || d <= this.max());
        }
    }
}
