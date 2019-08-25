package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class SpawnPlayerPacket implements ServerPacket {

    public int entityId;
    public UUID playerUuid;
    public Position position;
    public Buffer metadata;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putLong(playerUuid.getMostSignificantBits());
        buffer.putLong(playerUuid.getLeastSignificantBits());
        buffer.putDouble(position.getX());
        buffer.putDouble(position.getY());
        buffer.putDouble(position.getZ());
        buffer.putByte((byte) (position.getYaw() * 256f / 360f));
        buffer.putByte((byte) (position.getPitch() * 256f / 360f));
        if (metadata != null) {
            buffer.putBuffer(metadata);
        } else {
            buffer.putByte((byte) 0xff);
        }
    }

    @Override
    public int getId() {
        return 0x05;
    }
}
