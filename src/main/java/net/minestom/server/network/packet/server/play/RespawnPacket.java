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
        //TODO add api
        writer.writeSizedString("test:normal");
        writer.writeSizedString("test:spawn");
        writer.writeLong(hashedSeed);
        writer.writeByte(gameMode.getId());
        writer.writeByte(gameMode.getId()); // Hardcore flag not included
        //debug
        writer.writeBoolean(false);
        //is flat
        writer.writeBoolean(true);
        //copy meta
        writer.writeBoolean(true);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESPAWN;
    }
}
