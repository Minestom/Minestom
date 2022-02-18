package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SpawnLivingEntityPacket(int entityId, @NotNull UUID entityUuid, int entityType,
                                      @NotNull Pos position, float headPitch,
                                      short velocityX, short velocityY, short velocityZ) implements ServerPacket {
    public SpawnLivingEntityPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readUuid(), reader.readVarInt(),
                new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                        reader.readByte() * 360f / 256f,
                        reader.readByte() * 360f / 256f), reader.readByte() * 360f / 256f,
                reader.readShort(), reader.readShort(), reader.readShort());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(entityUuid);
        writer.writeVarInt(entityType);

        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());

        writer.writeByte((byte) (position.yaw() * 256 / 360));
        writer.writeByte((byte) (position.pitch() * 256 / 360));
        writer.writeByte((byte) (headPitch * 256 / 360));

        writer.writeShort(velocityX);
        writer.writeShort(velocityY);
        writer.writeShort(velocityZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_LIVING_ENTITY;
    }
}
