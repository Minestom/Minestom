package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public class SpawnExperienceOrbPacket implements ServerPacket {

    public int entityId;
    public Pos position;
    public short expCount;

    public SpawnExperienceOrbPacket() {
        position = Pos.ZERO;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());
        writer.writeShort(expCount);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        position = new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble());
        expCount = reader.readShort();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_EXPERIENCE_ORB;
    }
}
