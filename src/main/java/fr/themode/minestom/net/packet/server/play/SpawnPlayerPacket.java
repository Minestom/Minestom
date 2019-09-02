package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;
import simplenet.packet.Packet;

import java.util.UUID;
import java.util.function.Consumer;

public class SpawnPlayerPacket implements ServerPacket {

    public int entityId;
    public UUID playerUuid;
    public Position position;
    public Consumer<Packet> metadataConsumer;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(playerUuid);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeByte((byte) (position.getYaw() * 256f / 360f));
        writer.writeByte((byte) (position.getPitch() * 256f / 360f));

        if (metadataConsumer != null) {
            writer.write(metadataConsumer);
        } else {
            writer.writeByte((byte) 0xff);
        }
    }

    @Override
    public int getId() {
        return 0x05;
    }
}
