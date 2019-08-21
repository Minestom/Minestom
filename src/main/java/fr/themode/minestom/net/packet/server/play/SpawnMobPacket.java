package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class SpawnMobPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int entityType;
    public Position position;
    public float headPitch;
    public short velocityX, velocityY, velocityZ;
    // TODO metadata

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        Utils.writeUuid(buffer, entityUuid);
        Utils.writeVarInt(buffer, entityType);
        buffer.putDouble(position.getX());
        buffer.putDouble(position.getY());
        buffer.putDouble(position.getZ());
        buffer.putFloat(position.getYaw());
        buffer.putFloat(position.getPitch());
        buffer.putFloat(headPitch);
        buffer.putShort(velocityX);
        buffer.putShort(velocityY);
        buffer.putShort(velocityZ);
        buffer.putByte((byte) 0xff); // TODO metadata
    }

    @Override
    public int getId() {
        return 0x03;
    }
}
