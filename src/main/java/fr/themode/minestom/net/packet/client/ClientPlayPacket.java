package fr.themode.minestom.net.packet.client;

import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Player;

public abstract class ClientPlayPacket implements ClientPacket {

    public void process(Player player) {
        Main.getPacketListenerManager().process(this, player);
    }

}
