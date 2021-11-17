package net.minestom.server.network.packet.server;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public sealed interface SendablePacket
        permits ServerPacket, CachedPacket, FramedPacket {
}
