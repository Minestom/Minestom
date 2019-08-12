package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Block;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class ClientPlayerBlockPlacementPacket implements ClientPlayPacket {

    public Hand hand;
    public Position position;
    public ClientPlayerDiggingPacket.BlockFace blockFace;
    public float cursorPositionX, cursorPositionY, cursorPositionZ;
    public boolean insideBlock;

    @Override
    public void process(Player player) {
        Instance instance = player.getInstance();
        if (instance == null)
            return;
        instance.setBlock(position.getX(), position.getY(), position.getZ(), new Block(2));
    }

    @Override
    public void read(Buffer buffer) {
        this.hand = Hand.values()[Utils.readVarInt(buffer)];
        this.position = Utils.readPosition(buffer);
        this.blockFace = ClientPlayerDiggingPacket.BlockFace.values()[Utils.readVarInt(buffer)];
        this.cursorPositionX = buffer.getFloat();
        this.cursorPositionY = buffer.getFloat();
        this.cursorPositionZ = buffer.getFloat();
        this.insideBlock = buffer.getBoolean();
    }

    public enum Hand {
        MAIN_HAND,
        OFF_HAND;
    }

}
