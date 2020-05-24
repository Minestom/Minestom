package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;

@FunctionalInterface
public interface PacketConsumer {
    void accept(Player player, PacketController packetController, ClientPlayPacket packet);
}
