package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

import static net.minestom.server.network.NetworkBuffer.BITSET;

public record FilterMask(@NotNull Type type, @NotNull BitSet mask) {
    public static final NetworkBuffer.Type<FilterMask> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, FilterMask value) {
            buffer.write(NetworkBuffer.Enum(Type.class), value.type);
            if (value.type == Type.PARTIALLY_FILTERED) {
                buffer.write(BITSET, value.mask);
            }
        }

        @Override
        public FilterMask read(@NotNull NetworkBuffer buffer) {
            Type type = buffer.read(NetworkBuffer.Enum(Type.class));
            BitSet mask = type == Type.PARTIALLY_FILTERED ? buffer.read(BITSET) : new BitSet();
            return new FilterMask(type, mask);
        }
    };

    public enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED
    }
}
