package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;

import java.util.UUID;

public class SpawnMobPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int entityType;
    public Position position;
    public float headPitch;
    public short velocityX, velocityY, velocityZ;
    public Buffer metadata;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(entityUuid);
        writer.writeVarInt(entityType);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeFloat(position.getYaw());
        writer.writeFloat(position.getPitch());
        writer.writeFloat(headPitch);
        writer.writeShort(velocityX);
        writer.writeShort(velocityY);
        writer.writeShort(velocityZ);
        if (metadata != null) {
            writer.writeBuffer(metadata);
        } else {
            writer.writeByte((byte) 0xff);
        }
    }

    @Override
    public int getId() {
        return 0x03;
    }
}
