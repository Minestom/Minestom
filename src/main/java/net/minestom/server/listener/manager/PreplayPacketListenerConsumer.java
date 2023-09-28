package net.minestom.server.listener.manager;

import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.player.PlayerConnection;

/**
 * Small convenient interface to use method references with {@link PacketListenerManager#setListener(Class, PreplayPacketListenerConsumer)}.
 *
 * @param <T> the packet type
 */
@FunctionalInterface
public interface PreplayPacketListenerConsumer<T extends ClientPreplayPacket> {
    void accept(T packet, PlayerConnection connection);
}
