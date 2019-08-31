package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.BlockPosition;

public class ClientPlayerBlockPlacementPacket extends ClientPlayPacket {

    public Player.Hand hand;
    public BlockPosition blockPosition;
    public ClientPlayerDiggingPacket.BlockFace blockFace;
    public float cursorPositionX, cursorPositionY, cursorPositionZ;
    public boolean insideBlock;

    @Override
    public void read(PacketReader reader) {
        this.hand = Player.Hand.values()[reader.readVarInt()];
        this.blockPosition = reader.readBlockPosition();
        this.blockFace = ClientPlayerDiggingPacket.BlockFace.values()[reader.readVarInt()];
        this.cursorPositionX = reader.readFloat();
        this.cursorPositionY = reader.readFloat();
        this.cursorPositionZ = reader.readFloat();
        this.insideBlock = reader.readBoolean();
    }

}
