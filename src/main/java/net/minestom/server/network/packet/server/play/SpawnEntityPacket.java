package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpawnEntityPacket implements ServerPacket {

    public int entityId;
    public UUID uuid;
    public int type;
    public Pos position;
    public int data;
    public short velocityX, velocityY, velocityZ;

    public SpawnEntityPacket() {
        uuid = new UUID(0, 0);
        position = Pos.ZERO;
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

        writer.writeInt(data);

        writer.writeShort(velocityX);
        writer.writeShort(velocityY);
        writer.writeShort(velocityZ);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        uuid = reader.readUuid();
        type = reader.readVarInt();

        position = new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                reader.readByte() * 360f / 256f,
                reader.readByte() * 360f / 256f);

        data = reader.readInt();

        velocityX = reader.readShort();
        velocityY = reader.readShort();
        velocityZ = reader.readShort();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_ENTITY;
    }
}
