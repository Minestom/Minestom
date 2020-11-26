package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityPositionAndRotationPacket implements ServerPacket {

    public int entityId;
    public short deltaX, deltaY, deltaZ;
    public float yaw, pitch;
    public boolean onGround;

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
    public int getId() {
        return ServerPacketIdentifier.ENTITY_POSITION_AND_ROTATION;
    }

    public static EntityPositionAndRotationPacket getPacket(int entityId,
                                                            @NotNull Position newPosition, @NotNull Position oldPosition,
                                                            boolean onGround) {
        EntityPositionAndRotationPacket entityPositionAndRotationPacket = new EntityPositionAndRotationPacket();
        entityPositionAndRotationPacket.entityId = entityId;
        entityPositionAndRotationPacket.deltaX = (short) ((newPosition.getX() * 32 - oldPosition.getX() * 32) * 128);
        entityPositionAndRotationPacket.deltaY = (short) ((newPosition.getY() * 32 - oldPosition.getY() * 32) * 128);
        entityPositionAndRotationPacket.deltaZ = (short) ((newPosition.getZ() * 32 - oldPosition.getZ() * 32) * 128);
        entityPositionAndRotationPacket.yaw = newPosition.getYaw();
        entityPositionAndRotationPacket.pitch = newPosition.getPitch();
        entityPositionAndRotationPacket.onGround = onGround;

        return entityPositionAndRotationPacket;
    }
}
