package net.minestom.server.listener;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerSpectateEvent;
import net.minestom.server.network.packet.client.play.ClientSpectatePacket;

import java.util.UUID;

public class SpectateListener {

    public static void listener(ClientSpectatePacket packet, Player player) {
        // Ignore if the player is not in spectator mode
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        final UUID targetUuid = packet.target();
        final Entity target = Entity.getEntity(targetUuid);

        // Check if the target is valid
        if (target == null || target == player) {
            return;
        }

        // Ignore if they're not attached to any instances
        if (target.getInstance() == null || player.getInstance() == null) {
            return;
        }

        // Ignore if they're not in the same instance. Vanilla actually allows for
        // cross-instance spectating, but it's not really a good idea for Minestom.
        if (target.getInstance().getUniqueId() != player.getInstance().getUniqueId()) {
            return;
        }

        // Despite the name of this packet being spectate, it is sent when the player
        // uses their hotbar to switch between entities, which actually performs a teleport
        // instead of a spectate.
        EventDispatcher.call(new PlayerSpectateEvent(player, target));
    }

}
