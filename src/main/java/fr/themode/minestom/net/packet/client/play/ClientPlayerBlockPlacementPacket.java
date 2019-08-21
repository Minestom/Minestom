package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Utils;

public class ClientPlayerBlockPlacementPacket extends ClientPlayPacket {

    public Hand hand;
    public BlockPosition blockPosition;
    public ClientPlayerDiggingPacket.BlockFace blockFace;
    public float cursorPositionX, cursorPositionY, cursorPositionZ;
    public boolean insideBlock;

    @Override
    public void read(Buffer buffer) {
        this.hand = Hand.values()[Utils.readVarInt(buffer)];
        this.blockPosition = Utils.readPosition(buffer);
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
