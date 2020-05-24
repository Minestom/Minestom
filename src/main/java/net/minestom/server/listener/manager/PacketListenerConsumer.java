package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;

@FunctionalInterface
public interface PacketListenerConsumer<T extends ClientPlayPacket> {
    void accept(T packet, Player player);
}
