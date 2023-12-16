package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ParticlePacket(int particleId, boolean longDistance,
                             double x, double y, double z,
                             float offsetX, float offsetY, float offsetZ,
                             float particleData, int particleCount, byte[] data) implements ServerPacket {
    public ParticlePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(BOOLEAN),
                reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE),
                reader.read(FLOAT), reader.read(FLOAT), reader.read(FLOAT),
                reader.read(FLOAT), reader.read(INT), reader.read(RAW_BYTES));
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
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.PARTICLE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
