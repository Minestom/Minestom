package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class ExplosionPacket implements ServerPacket {

    public float x, y, z;
    public float radius; // UNUSED
    public byte[] records;
    public float playerMotionX, playerMotionY, playerMotionZ;

    @Override
    public void write(Buffer buffer) {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(radius);
        buffer.putInt(records.length);
        for (byte record : records)
            buffer.putByte(record);
        buffer.putFloat(playerMotionX);
        buffer.putFloat(playerMotionY);
        buffer.putFloat(playerMotionZ);
    }

    @Override
    public int getId() {
        return 0x1C;
    }
}
