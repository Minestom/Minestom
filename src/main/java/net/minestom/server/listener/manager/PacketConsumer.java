package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to add a listener for incoming packets with {@link net.minestom.server.network.ConnectionManager#onPacketReceive(PacketConsumer)}.
 */
@FunctionalInterface
public interface PacketConsumer {

    /**
     * Called when a packet is received from the client.
     *
     * @param player           the player who sent the packet
     * @param packetController the packet controller, used to cancel or control which listener will be called
     * @param packet           the received packet
     */
    void accept(@NotNull Player player, @NotNull PacketController packetController, @NotNull ClientPlayPacket packet);
}
