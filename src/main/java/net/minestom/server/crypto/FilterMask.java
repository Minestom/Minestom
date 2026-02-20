package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;

import java.util.BitSet;

import static net.minestom.server.network.NetworkBuffer.BITSET;

public record FilterMask(Type type, BitSet mask) {
    public static final NetworkBuffer.Type<FilterMask> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, FilterMask value) {
            buffer.write(Type.NETWORK_TYPE, value.type);
            if (value.type == Type.PARTIALLY_FILTERED) {
                buffer.write(BITSET, value.mask);
            }
        }

        @Override
        public FilterMask read(NetworkBuffer buffer) {
            Type type = buffer.read(Type.NETWORK_TYPE);
            BitSet mask = type == Type.PARTIALLY_FILTERED ? buffer.read(BITSET) : new BitSet();
            return new FilterMask(type, mask);
        }
    };

    public FilterMask {
        mask = (BitSet) mask.clone();
    }

    public enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED;

        public static final NetworkBuffer.Type<Type> NETWORK_TYPE = NetworkBuffer.Enum(Type.class);
    }
}
