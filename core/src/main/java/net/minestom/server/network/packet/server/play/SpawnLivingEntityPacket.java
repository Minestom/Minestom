package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpawnLivingEntityPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int entityType;
    public Position position;
    public float headPitch;
    public short velocityX, velocityY, velocityZ;

    public SpawnLivingEntityPacket() {
        entityUuid = new UUID(0, 0);
        position = new Position();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(entityUuid);
        writer.writeVarInt(entityType);

        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());

        writer.writeByte((byte) (position.getYaw() * 256 / 360));
        writer.writeByte((byte) (position.getPitch() * 256 / 360));
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

        position = new Position(reader.readDouble(), reader.readDouble(), reader.readDouble());

        position.setYaw(reader.readByte() * 360f / 256f);
        position.setPitch(reader.readByte() * 360f / 256f);
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
