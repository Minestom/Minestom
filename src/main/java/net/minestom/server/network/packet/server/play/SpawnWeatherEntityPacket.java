package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class SpawnWeatherEntityPacket implements ServerPacket {

    public int entityId;
    public byte type;
    public double x, y, z;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte(type);
        writer.writeDouble(x);
        writer.writeDouble(y);
        writer.writeDouble(z);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_WEATHER_ENTITY;
    }
}
