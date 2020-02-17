package fr.themode.minestom.net.packet.client;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.entity.Player;

public abstract class ClientPlayPacket implements ClientPacket {

    public void process(Player player) {
        MinecraftServer.getPacketListenerManager().process(this, player);
    }

}
