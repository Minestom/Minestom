package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.particle.ParticleOption;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record ParticlePacket(int particleId, boolean longDistance, double x, double y, double z,
                             float offsetX, float offsetY, float offsetZ,
                             float particleData, int particleCount,
                             byte[] data) implements ServerPacket {

    public ParticlePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(BOOLEAN),
                reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE),
                reader.read(FLOAT), reader.read(FLOAT), reader.read(FLOAT),
                reader.read(FLOAT), reader.read(INT), reader.read(RAW_BYTES));
    }

    public ParticlePacket(int particleId, boolean longDistance, double x, double y, double z,
                          float offsetX, float offsetY, float offsetZ,
                          float particleData, int particleCount, @Nullable ParticleOption options) {

        this(particleId, longDistance, x, y, z, offsetX, offsetY, offsetZ, particleData, particleCount,
                Objects.isNull(options) ? new byte[0] : BinaryWriter.makeArray(options::write));
    }

    @Deprecated(forRemoval = true)
    public ParticlePacket {
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, particleId);
        writer.write(BOOLEAN, longDistance);
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, y);
        writer.write(DOUBLE, z);
        writer.write(FLOAT, offsetX);
        writer.write(FLOAT, offsetY);
        writer.write(FLOAT, offsetZ);
        writer.write(FLOAT, particleData);
        writer.write(INT, particleCount);

        writer.write(RAW_BYTES, data);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PARTICLE;
    }
}
