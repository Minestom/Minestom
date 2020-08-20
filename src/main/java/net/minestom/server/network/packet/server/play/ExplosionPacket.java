package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class ExplosionPacket implements ServerPacket {

    public float x, y, z;
    public float radius; // UNUSED
    public byte[] records;
    public float playerMotionX, playerMotionY, playerMotionZ;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeFloat(x);
        writer.writeFloat(y);
        writer.writeFloat(z);
        writer.writeFloat(radius);
        writer.writeInt(records.length/3); // each record is 3 bytes long
        for (byte record : records)
            writer.writeByte(record);
        writer.writeFloat(playerMotionX);
        writer.writeFloat(playerMotionY);
        writer.writeFloat(playerMotionZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EXPLOSION;
    }
}
