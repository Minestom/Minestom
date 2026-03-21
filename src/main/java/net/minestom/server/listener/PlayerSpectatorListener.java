package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerSpectateEntityEvent;
import net.minestom.server.event.player.PlayerTeleportToEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientSpectateEntityPacket;
import net.minestom.server.network.packet.client.play.ClientTeleportToEntityPacket;

import java.util.UUID;

public class PlayerSpectatorListener {

    public static void listener(ClientSpectateEntityPacket packet, Player player) {
        // Ignore if the player is not in spectator mode
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        final int targetId = packet.targetId();
        final Entity target = player.getInstance().getEntityById(targetId);

        // Check if the target is valid, and the use is allowed
        if (target == null || target == player || UseEntityListener.invalidUse(player, target)) {
            return;
        }

        EventDispatcher.call(new PlayerSpectateEntityEvent(player, target));
    }

    public static void listener(ClientTeleportToEntityPacket packet, Player player) {
        // Ignore if the player is not in spectator mode
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        final UUID targetUuid = packet.target();
        final Instance playerInstance = player.getInstance();
        Entity target = playerInstance.getEntityByUuid(targetUuid);

        // If the target is not found, try to find it in other instances
        if (target == null) {
            for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                if (instance == playerInstance) continue;
                target = instance.getEntityByUuid(targetUuid);
                if (target != null) break;
            }
        }

        // Check if the target is valid
        if (target == null || target == player) {
            return;
        }

        EventDispatcher.call(new PlayerTeleportToEntityEvent(player, target));
    }
}
