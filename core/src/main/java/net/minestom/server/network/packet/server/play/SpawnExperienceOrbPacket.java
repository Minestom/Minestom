package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SpawnExperienceOrbPacket implements ServerPacket {

    public int entityId;
    public Position position;
    public short expCount;

    public SpawnExperienceOrbPacket() {
        position = new Position();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeShort(expCount);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        position = new Position(reader.readDouble(), reader.readDouble(), reader.readDouble());
        expCount = reader.readShort();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_EXPERIENCE_ORB;
    }
}
