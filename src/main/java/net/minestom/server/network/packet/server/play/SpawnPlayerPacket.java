package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SpawnPlayerPacket(int entityId, @NotNull UUID playerUuid,
                                @NotNull Pos position) implements ServerPacket {
    public SpawnPlayerPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readUuid(),
                new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                        (reader.readByte() * 360f) / 256f, (reader.readByte() * 360f) / 256f));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(playerUuid);
        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());
        writer.writeByte((byte) (position.yaw() * 256f / 360f));
        writer.writeByte((byte) (position.pitch() * 256f / 360f));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_PLAYER;
    }
}
