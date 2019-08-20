package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class ClientPlayerDiggingPacket extends ClientPlayPacket {

    public Status status;
    public Position position;
    public BlockFace blockFace;

    @Override
    public void read(Buffer buffer) {
        this.status = Status.values()[Utils.readVarInt(buffer)];
        this.position = Utils.readPosition(buffer);
        this.blockFace = BlockFace.values()[Utils.readVarInt(buffer)];
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
