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
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(value -> hand = Player.Hand.values()[value]);
        reader.readBlockPosition(blockPosition1 -> blockPosition = blockPosition1);
        reader.readVarInt(value -> blockFace = ClientPlayerDiggingPacket.BlockFace.values()[value]);
        reader.readFloat(value -> cursorPositionX = value);
        reader.readFloat(value -> cursorPositionY = value);
        reader.readFloat(value -> cursorPositionZ = value);
        reader.readBoolean(value -> {
            insideBlock = value;
            callback.run();
        });
    }

}
