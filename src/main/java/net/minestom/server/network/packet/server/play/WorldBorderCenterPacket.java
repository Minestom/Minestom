package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.DOUBLE;

public record WorldBorderCenterPacket(double x, double z) implements ServerPacket.Play {
    public WorldBorderCenterPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(DOUBLE), reader.read(DOUBLE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, z);
    }

}
