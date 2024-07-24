package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record BlockChangePacket(@NotNull Point blockPosition, int blockStateId) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<BlockChangePacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, BlockChangePacket::blockPosition,
            VAR_INT, BlockChangePacket::blockStateId,
            BlockChangePacket::new);

    public BlockChangePacket(@NotNull Point blockPosition, @NotNull Block block) {
        this(blockPosition, block.stateId());
    }
}
