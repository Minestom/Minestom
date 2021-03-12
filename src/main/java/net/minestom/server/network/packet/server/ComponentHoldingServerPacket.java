package net.minestom.server.network.packet.server;

import net.minestom.server.adventure.ComponentHolder;

/**
 * A server packet that can hold components.
 */
public interface ComponentHoldingServerPacket extends ServerPacket, ComponentHolder<ServerPacket> { }
