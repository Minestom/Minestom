package fr.themode.minestom.listener;

import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientChatMessagePacket;

public class ChatMessageListener {

    public static void listener(ClientChatMessagePacket packet, Player player) {
        String message = packet.message;
        Main.getConnectionManager().getOnlinePlayers().forEach(p -> p.sendMessage(String.format("<%s> %s", player.getUsername(), message)));

    }

}
