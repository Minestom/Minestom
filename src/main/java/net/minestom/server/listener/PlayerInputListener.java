package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientInputPacket;

public class PlayerInputListener {

    public static void listener(ClientInputPacket packet, Player player) {
        player.refreshInput(
                packet.forward(), packet.backward(),
                packet.left(), packet.right(),
                packet.jump(),
                packet.shift(),
                packet.sprint()
        );
    }

}
