package net.minestom.server.network.packet.client;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;

public abstract class ClientPlayPacket implements ClientPacket {

    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();

    public void process(Player player) {
        PACKET_LISTENER_MANAGER.process(this, player);
    }

}
