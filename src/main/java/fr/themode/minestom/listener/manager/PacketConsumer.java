package fr.themode.minestom.listener.manager;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPacket;

@FunctionalInterface
public interface PacketConsumer {
    // Cancel the packet if return true
    boolean accept(Player player, ClientPacket packet);
}
