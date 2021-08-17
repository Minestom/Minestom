package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ExplosionPacket implements ServerPacket {

    public float x, y, z;
    public float radius; // UNUSED
    public byte[] records = new byte[0];
    public float playerMotionX, playerMotionY, playerMotionZ;

    public ExplosionPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(x);
        writer.writeFloat(y);
        writer.writeFloat(z);
        writer.writeFloat(radius);
        writer.writeVarInt(records.length / 3); // each record is 3 bytes long
        for (byte record : records)
            writer.writeByte(record);
        writer.writeFloat(playerMotionX);
        writer.writeFloat(playerMotionY);
        writer.writeFloat(playerMotionZ);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        x = reader.readFloat();
        y = reader.readFloat();
        z = reader.readFloat();
        radius = reader.readFloat();
        int recordCount = reader.readVarInt() * 3;
        records = reader.readBytes(recordCount);
        playerMotionX = reader.readFloat();
        playerMotionY = reader.readFloat();
        playerMotionZ = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EXPLOSION;
    }
}
