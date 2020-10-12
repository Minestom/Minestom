package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;

public class EntityActionListener {

    public static void listener(ClientEntityActionPacket packet, Player player) {
        ClientEntityActionPacket.Action action = packet.action;
        switch (action) {
            case START_SNEAKING:
                player.setSneaking(true);
                break;
            case STOP_SNEAKING:
                player.setSneaking(false);
                break;
            case START_SPRINTING:
                player.setSprinting(true);
                break;
            case STOP_SPRINTING:
                player.setSprinting(false);
                break;
            // TODO do remaining actions
        }
    }
}
