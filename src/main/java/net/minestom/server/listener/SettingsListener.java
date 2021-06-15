package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerSettingsChangeEvent;
import net.minestom.server.network.packet.client.play.ClientSettingsPacket;

public class SettingsListener {

    public static void listener(ClientSettingsPacket packet, Player player) {
        Player.PlayerSettings settings = player.getSettings();
        settings.refresh(packet.locale, packet.viewDistance, packet.chatMessageType, packet.chatColors, packet.displayedSkinParts, packet.mainHand);

        PlayerSettingsChangeEvent playerSettingsChangeEvent = new PlayerSettingsChangeEvent(player);
        EventDispatcher.call(playerSettingsChangeEvent);
    }

}
