package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class SpawnObjectPacket implements ServerPacket {

    public int entityId;
    public UUID uuid;
    public int type;
    public double x, y, z;
    public float yaw, pitch;
    public int data;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        Utils.writeUuid(buffer, uuid);
        Utils.writeVarInt(buffer, type);
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putFloat(yaw);
        buffer.putFloat(pitch);
        buffer.putInt(data);
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
