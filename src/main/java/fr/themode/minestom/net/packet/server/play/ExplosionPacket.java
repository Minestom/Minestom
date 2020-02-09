package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class ExplosionPacket implements ServerPacket {

    public float x, y, z;
    public float radius; // UNUSED
    public byte[] records;
    public float playerMotionX, playerMotionY, playerMotionZ;

    @Override
    public void write(PacketWriter writer) {
        writer.writeFloat(x);
        writer.writeFloat(y);
        writer.writeFloat(z);
        writer.writeFloat(radius);
        writer.writeInt(records.length);
        for (byte record : records)
            writer.writeByte(record);
        writer.writeFloat(playerMotionX);
        writer.writeFloat(playerMotionY);
        writer.writeFloat(playerMotionZ);
    }

    @Override
    public int getId() {
        return 0x1D;
    }
}
