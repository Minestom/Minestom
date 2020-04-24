package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket;

public class PlayerVehicleListener {

    public static void steerVehicleListener(ClientSteerVehiclePacket packet, Player player) {
        byte flags = packet.flags;
        boolean jump = (flags & 0x1) != 0;
        boolean unmount = (flags & 0x2) != 0;
        player.refreshVehicleSteer(packet.sideways, packet.forward, jump, unmount);
    }

}
