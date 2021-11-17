package net.minestom.server.network.packet.server;

public sealed interface SendablePacket
        permits ServerPacket, CachedPacket, FramedPacket, LazyPacket {
}
