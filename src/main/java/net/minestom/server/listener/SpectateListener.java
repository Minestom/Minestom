package net.minestom.server.listener;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientSpectatePacket;

import java.util.UUID;

public class SpectateListener {

    public static void listener(ClientSpectatePacket packet, Player player) {
        final UUID targetUuid = packet.target();
        final Entity target = Entity.getEntity(targetUuid);

        // Check if the target is valid
        if (target == null || target == player) {
            return;
        }

        if (target.getInstance() == null || player.getInstance() == null) {
            return;
        }

        if (target.getInstance().getUniqueId() != player.getInstance().getUniqueId()) {
            return;
        }

        // Despite the name of this packet being spectate, it is sent when the player
        // uses their hotbar to switch between entities, which actually performs a teleport
        // instead of a spectate.
        player.teleport(target.getPosition());
    }

}
