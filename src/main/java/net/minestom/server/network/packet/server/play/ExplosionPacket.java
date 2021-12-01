package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ExplosionPacket(float x, float y, float z, float radius, byte @NotNull [] records,
                              float playerMotionX, float playerMotionY, float playerMotionZ) implements ServerPacket {
    public ExplosionPacket(BinaryReader reader) {
        this(reader.readFloat(), reader.readFloat(), reader.readFloat(),
                reader.readFloat(), reader.readBytes(reader.readVarInt() * 3),
                reader.readFloat(), reader.readFloat(), reader.readFloat());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(x);
        writer.writeFloat(y);
        writer.writeFloat(z);
        writer.writeFloat(radius);
        writer.writeVarInt(records.length / 3); // each record is 3 bytes long
        writer.writeBytes(records);
        writer.writeFloat(playerMotionX);
        writer.writeFloat(playerMotionY);
        writer.writeFloat(playerMotionZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EXPLOSION;
    }
}
