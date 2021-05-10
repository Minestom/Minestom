package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityVelocityPacket implements ServerPacket {

    public int entityId;
    public short velocityX, velocityY, velocityZ;

    public EntityVelocityPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeShort(velocityX);
        writer.writeShort(velocityY);
        writer.writeShort(velocityZ);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        velocityX = reader.readShort();
        velocityY = reader.readShort();
        velocityZ = reader.readShort();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_VELOCITY;
    }
}
