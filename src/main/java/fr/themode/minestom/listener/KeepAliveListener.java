package fr.themode.minestom.listener;

import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientKeepAlivePacket;
import fr.themode.minestom.net.packet.server.play.DisconnectPacket;

public class KeepAliveListener {

    public static void listener(ClientKeepAlivePacket packet, Player player) {
        if (packet.id != player.getLastKeepAlive()) {
            player.getPlayerConnection().sendPacket(new DisconnectPacket(Chat.rawText("Bad Keep Alive packet")));
            player.getPlayerConnection().getConnection().close();
        }
    }

}
