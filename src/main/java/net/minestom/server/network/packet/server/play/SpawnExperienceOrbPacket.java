package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

public class SpawnExperienceOrbPacket implements ServerPacket {

    public int entityId;
    public Position position;
    public short expCount;

    @Override
    public void write(BinaryWriter writer) {
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
