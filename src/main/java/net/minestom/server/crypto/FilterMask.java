package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.BitSet;

import static net.minestom.server.network.NetworkBuffer.BITSET;

public record FilterMask(Type type, BitSet mask) {
    @SuppressWarnings("unchecked")
    public static final NetworkBuffer.Type<FilterMask> SERIALIZER = NetworkBuffer.Type.tagged(
            NetworkBuffer.Enum(Type.class), FilterMask::type,
            type -> switch (type) {
                case PASS_THROUGH -> NetworkBufferTemplate.template(new FilterMask(Type.PASS_THROUGH, new BitSet()));
                case FULLY_FILTERED ->
                        NetworkBufferTemplate.template(new FilterMask(Type.FULLY_FILTERED, new BitSet()));
                case PARTIALLY_FILTERED -> (NetworkBuffer.Type<FilterMask>) NetworkBufferTemplate.template(
                        BITSET, FilterMask::mask,
                        mask -> new FilterMask(Type.PARTIALLY_FILTERED, mask));
            }
    );

    public enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED
    }
}
