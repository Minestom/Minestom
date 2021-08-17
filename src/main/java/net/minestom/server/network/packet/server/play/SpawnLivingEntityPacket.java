package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpawnLivingEntityPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int entityType;
    public Pos position;
    public float headPitch;
    public short velocityX, velocityY, velocityZ;

    public SpawnLivingEntityPacket() {
        entityUuid = new UUID(0, 0);
        position = Pos.ZERO;
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
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        entityUuid = reader.readUuid();
        entityType = reader.readVarInt();

        position = new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                reader.readByte() * 360f / 256f,
                reader.readByte() * 360f / 256f);
        headPitch = reader.readByte() * 360f / 256f;

        velocityX = reader.readShort();
        velocityY = reader.readShort();
        velocityZ = reader.readShort();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_LIVING_ENTITY;
    }
}
