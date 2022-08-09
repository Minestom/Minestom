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
        if (target == null || target == player)
            return;

        // Check if the target is in a different instance
        if (target.getInstance() != player.getInstance()) {
            //noinspection ConstantConditions
            player.setInstance(target.getInstance()).thenRun(() -> player.spectate(target));
            return;
        }
        player.spectate(target);
    }

}
