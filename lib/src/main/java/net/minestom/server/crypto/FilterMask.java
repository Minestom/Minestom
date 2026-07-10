package net.minestom.server.crypto;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.BitSet;
import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.BITSET;

public record FilterMask(Type type, BitSet mask) {
    public static final NetworkBuffer.Type<FilterMask> SERIALIZER = NetworkBuffer.Tagged(
            NetworkBuffer.Enum(Type.class), FilterMask::type,
            Map.of(
                    Type.PASS_THROUGH, NetworkBufferTemplate.template(new FilterMask(Type.PASS_THROUGH, new BitSet())),
                    Type.FULLY_FILTERED, NetworkBufferTemplate.template(new FilterMask(Type.FULLY_FILTERED, new BitSet())),
                    Type.PARTIALLY_FILTERED, NetworkBufferTemplate.template(
                            BITSET, FilterMask::mask,
                            mask -> new FilterMask(Type.PARTIALLY_FILTERED, mask))
            )
    );

    public FilterMask {
        mask = (BitSet) mask.clone();
    }

    public enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED
    }
}
