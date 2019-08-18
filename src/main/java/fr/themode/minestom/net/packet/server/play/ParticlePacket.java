package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class ParticlePacket implements ServerPacket {

    public int particleId;
    public boolean longDistance;
    public float x, y, z;
    public float offsetX, offsetY, offsetZ;
    public float particleData;
    public int particleCount;
    // TODO data

    @Override
    public void write(Buffer buffer) {
        buffer.putInt(particleId);
        buffer.putBoolean(longDistance);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(offsetX);
        buffer.putFloat(offsetY);
        buffer.putFloat(offsetZ);
        buffer.putFloat(particleData);
        buffer.putInt(particleCount);
        Utils.writeVarInt(buffer, 1);
    }

    @Override
    public int getId() {
        return 0x23;
    }
}
