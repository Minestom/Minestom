package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerStartSneakingEvent;
import net.minestom.server.event.player.PlayerStartSprintingEvent;
import net.minestom.server.event.player.PlayerStopSneakingEvent;
import net.minestom.server.event.player.PlayerStopSprintingEvent;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;

public class EntityActionListener {

    public static void listener(ClientEntityActionPacket packet, Player player) {
        ClientEntityActionPacket.Action action = packet.action;
        switch (action) {
            case START_SNEAKING:
                EntityActionListener.setSneaking(player, true);
                break;
            case STOP_SNEAKING:
                EntityActionListener.setSneaking(player, false);
                break;
            case START_SPRINTING:
                EntityActionListener.setSprinting(player, true);
                break;
            case STOP_SPRINTING:
                EntityActionListener.setSprinting(player, false);
                break;
            // TODO do remaining actions
        }
    }

    private static void setSneaking(Player player, boolean sneaking) {
        boolean oldState = player.isSneaking();

        player.setSneaking(sneaking);

        if (oldState != sneaking) {
            if (sneaking) {
                player.callEvent(PlayerStartSneakingEvent.class, new PlayerStartSneakingEvent(player));
            } else {
                player.callEvent(PlayerStopSneakingEvent.class, new PlayerStopSneakingEvent(player));
            }
        }
    }

    private static void setSprinting(Player player, boolean sprinting) {
        boolean oldState = player.isSprinting();

        player.setSprinting(sprinting);

        if (oldState != sprinting) {
            if (sprinting) {
                player.callEvent(PlayerStartSprintingEvent.class, new PlayerStartSprintingEvent(player));
            } else {
                player.callEvent(PlayerStopSprintingEvent.class, new PlayerStopSprintingEvent(player));
            }
        }
    }
}
