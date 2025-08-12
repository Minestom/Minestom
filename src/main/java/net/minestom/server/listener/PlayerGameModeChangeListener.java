package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;
import net.minestom.server.network.packet.client.play.ClientChangeGameModePacket;

public final class PlayerGameModeChangeListener {

    public static void listener(ClientChangeGameModePacket packet, Player player) {
        PlayerGameModeRequestEvent playerGameModeRequestEvent = new PlayerGameModeRequestEvent(player, packet.gameMode());
        EventDispatcher.call(playerGameModeRequestEvent);
    }
}
