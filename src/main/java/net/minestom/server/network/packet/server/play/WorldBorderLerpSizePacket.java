package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.DOUBLE;
import static net.minestom.server.network.NetworkBuffer.VAR_LONG;

public record WorldBorderLerpSizePacket(double oldDiameter, double newDiameter, long speed) implements ServerPacket {
    public WorldBorderLerpSizePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(VAR_LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(DOUBLE, oldDiameter);
        writer.write(DOUBLE, newDiameter);
        writer.write(VAR_LONG, speed);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.WORLD_BORDER_LERP_SIZE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
