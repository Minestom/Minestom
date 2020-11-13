package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to add a listener for incoming/outgoing packets with
 * {@link ConnectionManager#onPacketReceive(PacketConsumer)} and {@link ConnectionManager#onPacketSend(PacketConsumer)}.
 *
 * @param <T> the packet type
 */
@FunctionalInterface
public interface PacketConsumer<T> {

    /**
     * Called when a packet is received/sent from/to a client.
     *
     * @param player           the player concerned by the packet
     * @param packetController the packet controller, can be used to cancel the packet
     * @param packet           the packet
     */
    void accept(@NotNull Player player, @NotNull PacketController packetController, @NotNull T packet);
}
