package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.utils.Position;

public class SpawnExperienceOrbPacket implements ServerPacket {

    public int entityId;
    public Position position;
    public short expCount;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeShort(expCount);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_EXPERIENCE_ORB;
    }
}
