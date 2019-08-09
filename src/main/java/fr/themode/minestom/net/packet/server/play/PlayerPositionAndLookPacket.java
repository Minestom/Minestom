package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class PlayerPositionAndLookPacket implements ServerPacket {

    public double x, y, z;
    public float yaw, pitch;
    public byte flags;
    public int teleportId;


    @Override
    public void write(Buffer buffer) {
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putFloat(yaw);
        buffer.putFloat(pitch);
        buffer.putBytes(flags);
        Utils.writeVarInt(buffer, teleportId);
    }

    @Override
    public int getId() {
        return 0x35;
    }
}