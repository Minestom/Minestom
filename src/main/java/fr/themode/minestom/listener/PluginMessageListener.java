package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.PlayerPluginMessageEvent;
import fr.themode.minestom.net.packet.client.play.ClientPluginMessagePacket;

public class PluginMessageListener {

    public static void listener(ClientPluginMessagePacket packet, Player player) {
        PlayerPluginMessageEvent pluginMessageEvent = new PlayerPluginMessageEvent(packet.identifier, packet.data);
        player.callEvent(PlayerPluginMessageEvent.class, pluginMessageEvent);
    }

}
