package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public class EntityPositionAndRotationPacket implements ServerPacket {

    public int entityId;
    public short deltaX, deltaY, deltaZ;
    public float yaw, pitch;
    public boolean onGround;

    public EntityPositionAndRotationPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeShort(deltaX);
        writer.writeShort(deltaY);
        writer.writeShort(deltaZ);
        writer.writeByte((byte) (yaw * 256 / 360));
        writer.writeByte((byte) (pitch * 256 / 360));
        writer.writeBoolean(onGround);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        deltaX = reader.readShort();
        deltaY = reader.readShort();
        deltaZ = reader.readShort();
        yaw = reader.readByte() * 360f / 256f;
        pitch = reader.readByte() * 360f / 256f;
        onGround = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_POSITION_AND_ROTATION;
    }

    public static EntityPositionAndRotationPacket getPacket(int entityId,
                                                            @NotNull Pos newPosition, @NotNull Pos oldPosition,
                                                            boolean onGround) {
        EntityPositionAndRotationPacket entityPositionAndRotationPacket = new EntityPositionAndRotationPacket();
        entityPositionAndRotationPacket.entityId = entityId;
        entityPositionAndRotationPacket.deltaX = (short) ((newPosition.x() * 32 - oldPosition.x() * 32) * 128);
        entityPositionAndRotationPacket.deltaY = (short) ((newPosition.y() * 32 - oldPosition.y() * 32) * 128);
        entityPositionAndRotationPacket.deltaZ = (short) ((newPosition.z() * 32 - oldPosition.z() * 32) * 128);
        entityPositionAndRotationPacket.yaw = newPosition.yaw();
        entityPositionAndRotationPacket.pitch = newPosition.pitch();
        entityPositionAndRotationPacket.onGround = onGround;

        return entityPositionAndRotationPacket;
    }
}
