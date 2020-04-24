package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.world.Dimension;
import net.minestom.server.world.LevelType;

public class RespawnPacket implements ServerPacket {

    public Dimension dimension;
    public long hashedSeed;
    public GameMode gameMode;
    public LevelType levelType;

    @Override
    public void write(PacketWriter writer) {
        writer.writeInt(dimension.getId());
        writer.writeLong(hashedSeed);
        writer.writeByte(gameMode.getId()); // Hardcore flag not included
        writer.writeSizedString(levelType.getType());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESPAWN;
    }
}
