package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record SpawnExperienceOrbPacket(int entityId,
                                       @NotNull Pos position, short expCount) implements ServerPacket {
    public SpawnExperienceOrbPacket(BinaryReader reader) {
        this(reader.readVarInt(),
                new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble()), reader.readShort());
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
    public int getId() {
        return ServerPacketIdentifier.SPAWN_EXPERIENCE_ORB;
    }
}
