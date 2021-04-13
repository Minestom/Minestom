package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityPositionPacket implements ServerPacket {

    public int entityId;
    public short deltaX, deltaY, deltaZ;
    public boolean onGround;

    public EntityPositionPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeShort(deltaX);
        writer.writeShort(deltaY);
        writer.writeShort(deltaZ);
        writer.writeBoolean(onGround);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        deltaX = reader.readShort();
        deltaY = reader.readShort();
        deltaZ = reader.readShort();
        onGround = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_POSITION;
    }

    @NotNull
    public static EntityPositionPacket getPacket(int entityId,
                                                 @NotNull Position newPosition, @NotNull Position oldPosition,
                                                 boolean onGround) {
        EntityPositionPacket entityPositionPacket = new EntityPositionPacket();
        entityPositionPacket.entityId = entityId;
        entityPositionPacket.deltaX = (short) ((newPosition.getX() * 32 - oldPosition.getX() * 32) * 128);
        entityPositionPacket.deltaY = (short) ((newPosition.getY() * 32 - oldPosition.getY() * 32) * 128);
        entityPositionPacket.deltaZ = (short) ((newPosition.getZ() * 32 - oldPosition.getZ() * 32) * 128);
        entityPositionPacket.onGround = onGround;

        return entityPositionPacket;
    }
}
