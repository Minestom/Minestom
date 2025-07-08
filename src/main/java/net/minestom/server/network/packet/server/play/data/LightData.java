package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.BITSET;
import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;

public record LightData(
        @NotNull BitSet skyMask, @NotNull BitSet blockMask,
        @NotNull BitSet emptySkyMask, @NotNull BitSet emptyBlockMask,
        @NotNull List<byte[]> skyLight,
        @NotNull List<byte[]> blockLight
) {
    public LightData {
        skyMask = (BitSet) skyMask.clone();
        blockMask = (BitSet) blockMask.clone();
        emptySkyMask = (BitSet) emptySkyMask.clone();
        emptyBlockMask = (BitSet) emptyBlockMask.clone();
        skyLight = List.copyOf(skyLight);
        blockLight = List.copyOf(blockLight);
    }

    public static final int MAX_SECTIONS = 4096 / 16;

    public static final NetworkBuffer.Type<LightData> NETWORK_TYPE = NetworkBufferTemplate.template(
            BITSET, LightData::skyMask,
            BITSET, LightData::blockMask,
            BITSET, LightData::emptySkyMask,
            BITSET, LightData::emptyBlockMask,
            BYTE_ARRAY.list(MAX_SECTIONS), LightData::skyLight,
            BYTE_ARRAY.list(MAX_SECTIONS), LightData::blockLight,
            LightData::new
    );
}
