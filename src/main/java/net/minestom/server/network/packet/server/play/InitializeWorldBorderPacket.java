package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record InitializeWorldBorderPacket(double x, double z,
                                          double oldDiameter, double newDiameter, long speed,
                                          int portalTeleportBoundary, int warningTime,
                                          int warningBlocks) implements ServerPacket.Play {
    public InitializeWorldBorderPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(DOUBLE), reader.read(DOUBLE),
                reader.read(DOUBLE), reader.read(DOUBLE),
                reader.read(VAR_LONG), reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, z);
        writer.write(DOUBLE, oldDiameter);
        writer.write(DOUBLE, newDiameter);
        writer.write(VAR_LONG, speed);
        writer.write(VAR_INT, portalTeleportBoundary);
        writer.write(VAR_INT, warningTime);
        writer.write(VAR_INT, warningBlocks);
    }

}
