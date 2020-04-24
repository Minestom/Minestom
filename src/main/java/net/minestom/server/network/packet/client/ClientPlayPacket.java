package net.minestom.server.network.packet.client;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

public abstract class ClientPlayPacket implements ClientPacket {

    public void process(Player player) {
        MinecraftServer.getPacketListenerManager().process(this, player);
    }

}
