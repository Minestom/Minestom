package net.minestom.server.network.packet.client.play;

import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientPlayerDiggingPacket extends ClientPlayPacket {

    public Status status;
    public BlockPosition blockPosition;
    public BlockFace blockFace;

    @Override
    public void read(BinaryReader reader) {
        this.status = Status.values()[reader.readVarInt()];
        this.blockPosition = reader.readBlockPosition();
        this.blockFace = BlockFace.values()[reader.readByte()];
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
