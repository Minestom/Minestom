package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EntityVelocityPacket(int entityId, short velocityX, short velocityY,
                                   short velocityZ) implements ServerPacket {
    public EntityVelocityPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readShort(), reader.readShort(), reader.readShort());
    }

    public EntityVelocityPacket(int entityId, Point velocity) {
        this(entityId, (short) velocity.x(), (short) velocity.y(), (short) velocity.z());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeShort(velocityX);
        writer.writeShort(velocityY);
        writer.writeShort(velocityZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_VELOCITY;
    }
}
