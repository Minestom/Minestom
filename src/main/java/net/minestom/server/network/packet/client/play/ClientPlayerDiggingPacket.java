package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPlayerDiggingPacket(@NotNull Status status, @NotNull Point blockPosition,
                                        @NotNull BlockFace blockFace, int sequence) implements ClientPacket {
    public ClientPlayerDiggingPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(Status.class), reader.read(BLOCK_POSITION),
                BlockFace.getValues()[reader.read(BYTE)], reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(Status.class, status);
        writer.write(BLOCK_POSITION, blockPosition);
        writer.write(BYTE, (byte) blockFace.ordinal());
        writer.write(VAR_INT, sequence);
    }

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
