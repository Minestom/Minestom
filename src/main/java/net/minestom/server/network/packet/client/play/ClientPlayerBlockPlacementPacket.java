package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;

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
