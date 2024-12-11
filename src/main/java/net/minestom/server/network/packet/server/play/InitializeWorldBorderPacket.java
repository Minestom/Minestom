package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record InitializeWorldBorderPacket(double x, double z,
                                          double oldDiameter, double newDiameter, long speed,
                                          int portalTeleportBoundary, int warningTime,
                                          int warningBlocks) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<InitializeWorldBorderPacket> SERIALIZER = NetworkBufferTemplate.template(
            DOUBLE, InitializeWorldBorderPacket::x,
            DOUBLE, InitializeWorldBorderPacket::z,
            DOUBLE, InitializeWorldBorderPacket::oldDiameter,
            DOUBLE, InitializeWorldBorderPacket::newDiameter,
            VAR_LONG, InitializeWorldBorderPacket::speed,
            VAR_INT, InitializeWorldBorderPacket::portalTeleportBoundary,
            VAR_INT, InitializeWorldBorderPacket::warningTime,
            VAR_INT, InitializeWorldBorderPacket::warningBlocks,
            InitializeWorldBorderPacket::new);
}
