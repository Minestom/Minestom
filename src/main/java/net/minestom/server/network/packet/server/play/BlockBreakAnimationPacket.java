package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockBreakAnimationPacket(int entityId, @NotNull Point blockPosition,
                                        byte destroyStage) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<BlockBreakAnimationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, BlockBreakAnimationPacket::entityId,
            BLOCK_POSITION, BlockBreakAnimationPacket::blockPosition,
            BYTE, BlockBreakAnimationPacket::destroyStage,
            BlockBreakAnimationPacket::new);
}