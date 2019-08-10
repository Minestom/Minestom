package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class SpawnMobPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int entityType;
    public double x, y, z;
    public float yaw, pitch;
    public float headPitch;
    public short velocityX, velocityY, velocityZ;
    // TODO metadata

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putLong(entityUuid.getMostSignificantBits());
        buffer.putLong(entityUuid.getLeastSignificantBits());
        Utils.writeVarInt(buffer, entityType);
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putFloat(yaw);
        buffer.putFloat(pitch);
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
