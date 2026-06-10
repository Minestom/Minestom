package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

import java.util.List;

public final class EntityUtils {

    /**
     * @param vehicle the target vehicle
     * @param passenger the target passenger
     * @param passengerIndex the index of the passenger in the vehicle's passenger list
     * @return the height offset for the passenger of this vehicle
     */
    public static Point getPassengerPositionOffset(Entity vehicle, Entity passenger, int passengerIndex) {
        // Special cases: Boats, Camel
        if (vehicle.getEntityType() == EntityType.CAMEL) {
            if (passengerIndex == 0) {
                return new Vec(0, vehicle.getEntityType().height(), 0.5).rotateAroundY(Math.toRadians(vehicle.getHeadRotation() * -1));
            } else {
                return new Vec(0, vehicle.getEntityType().height(), -0.7).rotateAroundY(Math.toRadians(vehicle.getHeadRotation() * -1));
            }
        } else if (vehicle.getEntityType().name().contains("boat")) {
            if (passengerIndex == 0) {
                return new Vec(0, vehicle.getEntityType().height() * 0.33, 0.2).rotateAroundY(Math.toRadians(vehicle.getHeadRotation() * -1));
            } else {
                return new Vec(0, vehicle.getEntityType().height() * 0.33, -0.6).rotateAroundY(Math.toRadians(vehicle.getHeadRotation() * -1));
            }
        } else {
            // Passenger position offset is a combination of vehicle passenger position and passenger vehicle position (if it exists)
            List<Double> vehiclePassengerPos = vehicle.getEntityType().registry().entityAttachment("PASSENGER");
            Vec passengerOffset = vehiclePassengerPos != null ? new Vec(vehiclePassengerPos.get(0), vehiclePassengerPos.get(1), vehiclePassengerPos.get(2)) :
                    new Vec(0, vehicle.getEntityType().height(), 0);
            List<Double> passengerVehiclePos = passenger.getEntityType().registry().entityAttachment("VEHICLE");
            Vec vehicleOffset = passengerVehiclePos != null ? new Vec(passengerVehiclePos.get(0), passengerVehiclePos.get(1), passengerVehiclePos.get(2)) :
                    Vec.ZERO;
            return passengerOffset.sub(vehicleOffset);
        }
    }

    private EntityUtils() {}
}
