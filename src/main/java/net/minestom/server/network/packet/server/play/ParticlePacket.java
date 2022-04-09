package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ParticlePacket(int particleId, boolean longDistance,
                             double x, double y, double z,
                             float offsetX, float offsetY, float offsetZ,
                             float particleData, int particleCount, byte[] data) implements ServerPacket {
    public ParticlePacket(BinaryReader reader) {
        this(reader.readInt(), reader.readBoolean(),
                reader.readDouble(), reader.readDouble(), reader.readDouble(),
                reader.readFloat(), reader.readFloat(), reader.readFloat(),
                reader.readFloat(), reader.readInt(), reader.readRemainingBytes());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(particleId);
        writer.writeBoolean(longDistance);
        writer.writeDouble(x);
        writer.writeDouble(y);
        writer.writeDouble(z);
        writer.writeFloat(offsetX);
        writer.writeFloat(offsetY);
        writer.writeFloat(offsetZ);
        writer.writeFloat(particleData);
        writer.writeInt(particleCount);

        writer.writeBytes(data);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PARTICLE;
    }
}
