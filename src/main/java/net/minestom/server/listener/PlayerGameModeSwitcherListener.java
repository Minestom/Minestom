package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerGameModeSwitcherEvent;
import net.minestom.server.network.packet.client.play.ClientChangeGameModePacket;

public class PlayerGameModeSwitcherListener {

    public static void playerGameModeSwitcherListener(ClientChangeGameModePacket packet, Player player) {
        PlayerGameModeSwitcherEvent playerGameModeSwitcherEvent = new PlayerGameModeSwitcherEvent(player, packet.gameMode());
        EventDispatcher.call(playerGameModeSwitcherEvent);
    }
}
