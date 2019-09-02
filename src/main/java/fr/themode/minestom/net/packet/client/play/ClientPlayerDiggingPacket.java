package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.BlockPosition;

public class ClientPlayerDiggingPacket extends ClientPlayPacket {

    public Status status;
    public BlockPosition blockPosition;
    public BlockFace blockFace;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(value -> status = Status.values()[value]);
        reader.readBlockPosition(blockPosition1 -> blockPosition = blockPosition1);
        reader.readVarInt(value -> {
            blockFace = BlockFace.values()[value];
            callback.run();
        });
    }

    public enum Status {
        STARTED_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        UPDATE_ITEM_STATE,
        SWAP_ITEM_HAND;
    }

    public enum BlockFace {
        BOTTOM,
        TOP,
        NORTH,
        SOUTH,
        WEST,
        EAST;
    }

}
