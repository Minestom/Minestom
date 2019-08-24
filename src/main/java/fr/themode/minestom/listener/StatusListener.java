package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.PlayerRespawnEvent;
import fr.themode.minestom.net.packet.client.play.ClientStatusPacket;
import fr.themode.minestom.net.packet.server.play.RespawnPacket;

public class StatusListener {

    public static void listener(ClientStatusPacket packet, Player player) {
        switch (packet.action) {
            case PERFORM_RESPAWN:
                RespawnPacket respawnPacket = new RespawnPacket();
                respawnPacket.dimension = player.getDimension();
                respawnPacket.gameMode = player.getGameMode();
                respawnPacket.levelType = player.getLevelType();
                player.getPlayerConnection().sendPacket(respawnPacket);
                PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(player.getPosition());
                player.callEvent(PlayerRespawnEvent.class, respawnEvent);
                player.refreshIsDead(false);
                player.teleport(respawnEvent.getRespawnPosition());
                player.getInventory().update();
                break;
            case REQUEST_STATS:
                // TODO stats
                break;
        }
    }

}
