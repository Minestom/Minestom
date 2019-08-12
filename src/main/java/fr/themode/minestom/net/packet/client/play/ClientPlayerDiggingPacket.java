package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Block;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class ClientPlayerDiggingPacket implements ClientPlayPacket {

    public Status status;
    public Position position;
    public BlockFace blockFace;

    @Override
    public void process(Player player) {
        switch (status) {
            case STARTED_DIGGING:
                if (player.getGameMode() == GameMode.CREATIVE) {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        instance.setBlock(position.getX(), position.getY(), position.getZ(), new Block(0));
                    }
                }
                break;
        }
    }

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
