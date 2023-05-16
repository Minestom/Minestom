package net.minestom.server.listener;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.BoatMeta;
import net.minestom.server.network.packet.client.play.ClientSteerBoatPacket;
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket;
import net.minestom.server.network.packet.client.play.ClientVehicleMovePacket;

public class PlayerVehicleListener {

    public static void steerVehicleListener(ClientSteerVehiclePacket packet, Player player) {
        final byte flags = packet.flags();
        final boolean jump = (flags & 0x1) != 0;
        final boolean unmount = (flags & 0x2) != 0;
        player.refreshVehicleSteer(packet.sideways(), packet.forward(), jump, unmount);
    }

    public static void vehicleMoveListener(ClientVehicleMovePacket packet, Player player) {
        final Entity vehicle = player.getVehicle();
        if (vehicle == null)
            return;

        vehicle.refreshPosition(packet.position());

        // This packet causes weird screen distortion
        /*VehicleMovePacket vehicleMovePacket = new VehicleMovePacket();
        vehicleMovePacket.x = packet.x;
        vehicleMovePacket.y = packet.y;
        vehicleMovePacket.z = packet.z;
        vehicleMovePacket.yaw = packet.yaw;
        vehicleMovePacket.pitch = packet.pitch;
        player.getPlayerConnection().sendPacket(vehicleMovePacket);*/

    }

    public static void boatSteerListener(ClientSteerBoatPacket packet, Player player) {
        final Entity vehicle = player.getVehicle();
        /* The packet may have been received after already exiting the vehicle. */
        if (vehicle == null) return;
        if (!(vehicle.getEntityMeta() instanceof BoatMeta boat)) return;
        boat.setLeftPaddleTurning(packet.leftPaddleTurning());
        boat.setRightPaddleTurning(packet.rightPaddleTurning());
    }
}