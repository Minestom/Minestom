package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EntityPositionPacket(int entityId, short deltaX, short deltaY, short deltaZ, boolean onGround)
        implements ServerPacket {

    public EntityPositionPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readShort(), reader.readShort(), reader.readShort(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeShort(deltaX);
        writer.writeShort(deltaY);
        writer.writeShort(deltaZ);
        writer.writeBoolean(onGround);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_POSITION;
    }

    @NotNull
    public static EntityPositionPacket getPacket(int entityId,
                                                 @NotNull Pos newPosition, @NotNull Pos oldPosition,
                                                 boolean onGround) {
        final short deltaX = (short) ((newPosition.x() * 32 - oldPosition.x() * 32) * 128);
        final short deltaY = (short) ((newPosition.y() * 32 - oldPosition.y() * 32) * 128);
        final short deltaZ = (short) ((newPosition.z() * 32 - oldPosition.z() * 32) * 128);
        return new EntityPositionPacket(entityId, deltaX, deltaY, deltaZ, onGround);
    }
}
