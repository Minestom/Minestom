package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;

@FunctionalInterface
public interface PacketConsumer {
    // Cancel the packet if return true
    boolean accept(Player player, ClientPacket packet);
}
