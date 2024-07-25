package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.VAR_LONG_ARRAY;

public record MultiBlockChangePacket(long chunkSectionPosition, long[] blocks) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<MultiBlockChangePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, MultiBlockChangePacket::chunkSectionPosition,
            VAR_LONG_ARRAY.optional(), MultiBlockChangePacket::blocks,
            MultiBlockChangePacket::new);

    public MultiBlockChangePacket(int chunkX, int section, int chunkZ,
                                  long[] blocks) {
        this(((long) (chunkX & 0x3FFFFF) << 42) | (section & 0xFFFFF) | ((long) (chunkZ & 0x3FFFFF) << 20), blocks);
    }
}
