package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class SpawnPlayerPacket implements ServerPacket {

    public int entityId;
    public UUID playerUuid;
    public double x;
    public double y;
    public double z;
    // public float yaw;
    // public float pitch;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putLong(playerUuid.getMostSignificantBits());
        buffer.putLong(playerUuid.getLeastSignificantBits());
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.getData().writeByte(0);
        buffer.getData().writeByte(0);
        buffer.putByte((byte) 0xff); // TODO Metadata
    }

    @Override
    public int getId() {
        return 0x05;
    }
}
