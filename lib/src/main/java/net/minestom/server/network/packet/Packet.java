package net.minestom.server.network.packet;

/**
 * Marker for client and server packets.
 * <p>
 * Not sealed: the two permitted hierarchies ({@code ClientPacket}, {@code ServerPacket})
 * live in subpackages, and cross-package sealing requires a named module — :lib is a
 * classpath jar because it shares {@code net.minestom.server} packages with :framework.
 */
public interface Packet {
}
