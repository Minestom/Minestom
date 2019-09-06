package fr.themode.minestom.net.packet.server.play;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;

import java.util.UUID;
import java.util.function.Consumer;

public class SpawnMobPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int entityType;
    public Position position;
    public float headPitch;
    public short velocityX, velocityY, velocityZ;
    public Consumer<Packet> consumer;

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
        if (consumer != null) {
            writer.write(consumer);
        } else {
            writer.writeByte((byte) 0xff);
        }
    }

    @Override
    public int getId() {
        return 0x03;
    }
}
