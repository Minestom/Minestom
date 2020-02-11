package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.utils.Position;

import java.util.UUID;

public class SpawnObjectPacket implements ServerPacket {

    public int entityId;
    public UUID uuid;
    public int type;
    public Position position;
    public int data;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(uuid);
        writer.writeVarInt(type);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeFloat(position.getYaw());
        writer.writeFloat(position.getPitch());
        writer.writeInt(data);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_OBJECT;
    }
}
