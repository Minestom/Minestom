package net.minestom.server.listener;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientSpectatePacket;

import java.util.UUID;

public class SpectateListener {

    public static void listener(ClientSpectatePacket packet, Player player) {
        final UUID targetUuid = packet.targetUuid;

        // TODO no check is set to make sure that this doesn't actually break.
        final Entity target = Entity.getEntity((int) targetUuid.getLeastSignificantBits());

        // Check if the target is valid
        if (target == null || target == player)
            return;

        // TODO check if 'target' is in a different instance
        player.spectate(target);
    }

}
