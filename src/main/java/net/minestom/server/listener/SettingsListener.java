package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerSettingsChangeEvent;
import net.minestom.server.network.packet.client.play.ClientSettingsPacket;

public final class SettingsListener {
    public static void listener(ClientSettingsPacket packet, Player player) {
        Player.PlayerSettings settings = player.getSettings();
        final byte viewDistance = (byte) Math.abs(packet.viewDistance());
        settings.refresh(packet.locale(), viewDistance, packet.chatMessageType(), packet.chatColors(), packet.displayedSkinParts(), packet.mainHand(), packet.enableTextFiltering(), packet.allowsListing());
        EventDispatcher.call(new PlayerSettingsChangeEvent(player));
    }
}
