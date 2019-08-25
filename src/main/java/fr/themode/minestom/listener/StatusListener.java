package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientStatusPacket;

public class StatusListener {

    public static void listener(ClientStatusPacket packet, Player player) {
        switch (packet.action) {
            case PERFORM_RESPAWN:
                player.respawn();
                break;
            case REQUEST_STATS:
                // TODO stats
                break;
        }
    }

}
