package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPlayerBlockPlacementPacket(@NotNull PlayerHand hand, @NotNull Point blockPosition,
                                               @NotNull BlockFace blockFace,
                                               float cursorPositionX, float cursorPositionY, float cursorPositionZ,
                                               boolean insideBlock, int sequence) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlayerBlockPlacementPacket> SERIALIZER = NetworkBufferTemplate.template(
            Enum(PlayerHand.class), ClientPlayerBlockPlacementPacket::hand,
            BLOCK_POSITION, ClientPlayerBlockPlacementPacket::blockPosition,
            Enum(BlockFace.class), ClientPlayerBlockPlacementPacket::blockFace,
            FLOAT, ClientPlayerBlockPlacementPacket::cursorPositionX,
            FLOAT, ClientPlayerBlockPlacementPacket::cursorPositionY,
            FLOAT, ClientPlayerBlockPlacementPacket::cursorPositionZ,
            BOOLEAN, ClientPlayerBlockPlacementPacket::insideBlock,
            VAR_INT, ClientPlayerBlockPlacementPacket::sequence,
            ClientPlayerBlockPlacementPacket::new);
}
