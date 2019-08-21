package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientSettingsPacket;

public class SettingsListener {

    public static void listener(ClientSettingsPacket packet, Player player) {
        Player.PlayerSettings settings = player.getSettings();
        settings.refresh(packet.locale, packet.viewDistance, packet.chatMode, packet.chatColors, packet.displayedSkinParts, packet.mainHand);
    }

}
