package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerLeaveBedEvent;
import net.minestom.server.event.player.PlayerStartFlyingWithElytraEvent;
import net.minestom.server.event.player.PlayerStartSprintingEvent;
import net.minestom.server.event.player.PlayerStopSprintingEvent;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;

public class EntityActionListener {

    public static void listener(ClientEntityActionPacket packet, Player player) {
        switch (packet.action()) {
            case START_SPRINTING -> EntityActionListener.setSprinting(player, true);
            case STOP_SPRINTING -> EntityActionListener.setSprinting(player, false);
            case START_FLYING_ELYTRA -> EntityActionListener.startFlyingElytra(player);
            case LEAVE_BED -> EntityActionListener.onLeaveBed(player);

            // TODO do remaining actions
        }
    }

    private static void setSprinting(Player player, boolean sprinting) {
        boolean oldState = player.isSprinting();

        player.setSprinting(sprinting);

        if (oldState != sprinting) {
            if (sprinting) {
                EventDispatcher.call(new PlayerStartSprintingEvent(player));
            } else {
                EventDispatcher.call(new PlayerStopSprintingEvent(player));
            }
        }
    }

    private static void startFlyingElytra(Player player) {
        player.setFlyingWithElytra(true);
        EventDispatcher.call(new PlayerStartFlyingWithElytraEvent(player));
    }

    private static void onLeaveBed(Player player) {
        var event = new PlayerLeaveBedEvent(player);
        EventDispatcher.callCancellable(event, () -> {
            player.getLivingEntityMeta().setBedInWhichSleepingPosition(null);
            player.leaveBed();
        });
    }
}
