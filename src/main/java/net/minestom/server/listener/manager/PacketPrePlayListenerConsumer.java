package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.player.PlayerConnection;

/**
 * Small convenient interface to use method references with {@link PacketListenerManager#setListener(ConnectionState, Class, PacketPrePlayListenerConsumer)}.
 *
 * @param <T> the packet type
 */
@FunctionalInterface
public interface PacketPrePlayListenerConsumer<T extends ClientPacket> {
    void accept(T packet, PlayerConnection connection);
}
