package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPlayerDiggingPacket(Status status, Point blockPosition,
                                        BlockFace blockFace, int sequence) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientPlayerDiggingPacket> SERIALIZER = NetworkBufferTemplate.template(
            Status.NETWORK_TYPE, ClientPlayerDiggingPacket::status,
            BLOCK_POSITION, ClientPlayerDiggingPacket::blockPosition,
            NetworkBuffer.Enum(BlockFace.class), ClientPlayerDiggingPacket::blockFace,
            VAR_INT, ClientPlayerDiggingPacket::sequence,
            ClientPlayerDiggingPacket::new);

    public enum Status {
        STARTED_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        UPDATE_ITEM_STATE,
        SWAP_ITEM_HAND;

        public static final NetworkBuffer.Type<Status> NETWORK_TYPE = NetworkBuffer.Enum(Status.class);
    }
}
