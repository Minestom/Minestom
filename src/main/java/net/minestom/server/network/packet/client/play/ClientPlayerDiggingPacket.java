package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPlayerDiggingPacket(@NotNull Status status, @NotNull Point blockPosition,
                                        @NotNull BlockFace blockFace, int sequence) implements ClientPacket {

    public static NetworkBuffer.Type<ClientPlayerDiggingPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, ClientPlayerDiggingPacket value) {
            writer.writeEnum(Status.class, value.status);
            writer.write(BLOCK_POSITION, value.blockPosition);
            writer.write(BYTE, (byte) value.blockFace.ordinal());
            writer.write(VAR_INT, value.sequence);
        }

        @Override
        public ClientPlayerDiggingPacket read(@NotNull NetworkBuffer reader) {
            return new ClientPlayerDiggingPacket(reader.readEnum(Status.class), reader.read(BLOCK_POSITION),
                    BlockFace.values()[reader.read(BYTE)], reader.read(VAR_INT));
        }
    };

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
