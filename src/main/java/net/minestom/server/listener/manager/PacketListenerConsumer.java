package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;

/**
 * Small convenient interface to use method references with {@link PacketListenerManager#setListener(Class, PacketListenerConsumer)}.
 *
 * @param <T> the packet type
 */
@FunctionalInterface
public interface PacketListenerConsumer<T extends ClientPacket> {
    void accept(T packet, Player player);
}
