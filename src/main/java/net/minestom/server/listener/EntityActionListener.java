package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;

public class EntityActionListener {

    public static void listener(ClientEntityActionPacket packet, Player player) {
        ClientEntityActionPacket.Action action = packet.action;
        switch (action) {
            case START_SNEAKING:
                player.refreshSneaking(true);
                break;
            case STOP_SNEAKING:
                player.refreshSneaking(false);
                break;
            case START_SPRINTING:
                player.refreshSprinting(true);
                break;
            case STOP_SPRINTING:
                player.refreshSprinting(false);
                break;
            // TODO do remaining actions
        }
    }
}
