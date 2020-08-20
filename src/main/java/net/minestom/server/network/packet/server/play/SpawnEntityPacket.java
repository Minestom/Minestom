package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.UUID;

public class SpawnEntityPacket implements ServerPacket {

    public int entityId;
    public UUID uuid;
    public int type;
    public Position position;
    public int data;
    public short velocityX, velocityY, velocityZ;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(uuid);
        writer.writeVarInt(type);

        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());

        writer.writeByte((byte) (position.getYaw() * 256 / 360));
        writer.writeByte((byte) (position.getPitch() * 256 / 360));

        writer.writeInt(data);

        writer.writeShort(velocityX);
        writer.writeShort(velocityY);
        writer.writeShort(velocityZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_ENTITY;
    }
}
