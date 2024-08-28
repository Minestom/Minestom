package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPlayerDiggingPacket(@NotNull Status status, @NotNull Point blockPosition,
                                        @NotNull BlockFace blockFace, int sequence) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlayerDiggingPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(Status.class), ClientPlayerDiggingPacket::status,
            BLOCK_POSITION, ClientPlayerDiggingPacket::blockPosition,
            BYTE.transform(aByte -> BlockFace.values()[aByte], blockFace1 -> (byte) blockFace1.ordinal()), ClientPlayerDiggingPacket::blockFace,
            VAR_INT, ClientPlayerDiggingPacket::sequence,
            ClientPlayerDiggingPacket::new);

    public enum Status {
        STARTED_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        UPDATE_ITEM_STATE,
        SWAP_ITEM_HAND
    }
}
