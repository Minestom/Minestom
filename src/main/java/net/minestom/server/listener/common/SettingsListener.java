package net.minestom.server.listener.common;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerSettingsChangeEvent;
import net.minestom.server.network.packet.client.common.ClientSettingsPacket;

public final class SettingsListener {
    public static void listener(ClientSettingsPacket packet, Player player) {
        // Since viewDistance bounds checking is performed in the refresh function, it is not necessary to check it here
        player.refreshSettings(packet.settings());
        EventDispatcher.call(new PlayerSettingsChangeEvent(player));
    }
}
