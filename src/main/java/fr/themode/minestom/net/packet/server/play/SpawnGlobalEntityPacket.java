package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class SpawnGlobalEntityPacket implements ServerPacket {

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
        return ServerPacketIdentifier.SPAWN_GLOBAL_ENTITY;
    }
}
