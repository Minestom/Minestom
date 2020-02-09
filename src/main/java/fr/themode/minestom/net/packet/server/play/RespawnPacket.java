package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.world.Dimension;
import fr.themode.minestom.world.LevelType;

public class RespawnPacket implements ServerPacket {

    public Dimension dimension;
    public long hashedSeed;
    public GameMode gameMode;
    public LevelType levelType;

    @Override
    public void write(PacketWriter writer) {
        writer.writeInt(dimension.getId());
        writer.writeLong(hashedSeed);
        writer.writeByte((byte) gameMode.getId()); // Hardcore flag not included
        writer.writeSizedString(levelType.getType());
    }

    @Override
    public int getId() {
        return 0x3B;
    }
}
