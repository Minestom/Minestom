package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SpawnEntityPacket(int entityId, @NotNull UUID uuid, int type,
                                @NotNull Pos position, float headRot, int data,
                                short velocityX, short velocityY, short velocityZ) implements ServerPacket {
    public SpawnEntityPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readUuid(), reader.readVarInt(),
                new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                        reader.readByte() * 360f / 256f, reader.readByte() * 360f / 256f), reader.readByte() * 360f / 256f,
                reader.readVarInt(), reader.readShort(), reader.readShort(), reader.readShort());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(uuid);
        writer.writeVarInt(type);

        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());

        writer.writeByte((byte) (position.yaw() * 256 / 360));
        writer.writeByte((byte) (position.pitch() * 256 / 360));
        writer.writeByte((byte) (headRot * 256 / 360));

        writer.writeVarInt(data);

        writer.writeShort(velocityX);
        writer.writeShort(velocityY);
        writer.writeShort(velocityZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_ENTITY;
    }
}
