package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockActionPacket(@NotNull Point blockPosition, byte actionId,
                                byte actionParam, int blockId) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<BlockActionPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, BlockActionPacket::blockPosition,
            BYTE, BlockActionPacket::actionId,
            BYTE, BlockActionPacket::actionParam,
            VAR_INT, BlockActionPacket::blockId,
            BlockActionPacket::new);

    public BlockActionPacket(Point blockPosition, byte actionId, byte actionParam, Block block) {
        this(blockPosition, actionId, actionParam, block.id());
    }
}
