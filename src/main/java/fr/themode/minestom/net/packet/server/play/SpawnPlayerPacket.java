package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;

import java.util.UUID;

public class SpawnPlayerPacket implements ServerPacket {

    public int entityId;
    public UUID playerUuid;
    public Position position;
    public Buffer metadata;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(playerUuid);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeByte((byte) (position.getYaw() * 256f / 360f));
        writer.writeByte((byte) (position.getPitch() * 256f / 360f));

        if (metadata != null) {
            writer.writeBuffer(metadata);
        } else {
            writer.writeByte((byte) 0xff);
        }
    }

    @Override
    public int getId() {
        return 0x05;
    }
}
