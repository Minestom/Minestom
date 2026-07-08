package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.CamelMeta;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EntityUtils {

    /**
     * @param vehicle the target vehicle
     * @param passenger the target passenger
     * @param passengerIndex the index of the passenger in the vehicle's passenger list
     * @return the height offset for the passenger of this vehicle
     */
    public static Point getPassengerPositionOffset(Entity vehicle, Entity passenger, int passengerIndex) {
        // Special cases: Boats, Rafts, Camel, Happy Ghast
        if (vehicle.getEntityType() == EntityType.CAMEL || vehicle.getEntityType() == EntityType.CAMEL_HUSK) {
            List<Double> passengerVehiclePos = passenger.getEntityType().registry().entityAttachment("VEHICLE");
            Vec vehicleOffset = attachmentListToVec(passengerVehiclePos, Vec.ZERO);
            // Check pose
            boolean isSitting = vehicle.getPose() == EntityPose.SITTING;
            boolean isBaby = vehicle.getEntityMeta() instanceof CamelMeta camelMeta && camelMeta.isBaby();
            // Calculate height
            double yOffset = isBaby ? 0.09375 : 0.375;
            double height = isBaby ? 1.4 : vehicle.getEntityType().height() - yOffset;
            if (isSitting) {
                // Some magic constants, somewhat unavoidable since these are not stored in entity data
                if (isBaby) {
                    height -= 0.855;
                } else {
                    height -= 1.23f;
                }
            }

            double animalOffset = isEntityAnimal(passenger.getEntityType()) ? 0.2 : 0;
            if (passengerIndex == 0) {
                return new Vec(0, height, 0.5 + animalOffset).sub(vehicleOffset).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
            } else {
                return new Vec(0, height, -0.7 + animalOffset).sub(vehicleOffset).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
            }
        } else if (vehicle.getEntityType() == EntityType.HAPPY_GHAST) {
            List<List<Double>> positions = vehicle.getEntityType().registry().entityAttachments("PASSENGER");
            Vec passengerOffset = positions != null ? attachmentListToVec(positions.get(Math.min(passengerIndex, positions.size())), new Vec(0, vehicle.getEntityType().height(), 0)) : Vec.ZERO;
            List<Double> passengerVehiclePos = passenger.getEntityType().registry().entityAttachment("VEHICLE");
            Vec vehicleOffset = attachmentListToVec(passengerVehiclePos, Vec.ZERO);
            return passengerOffset.sub(vehicleOffset).rotateAroundY(Math.toRadians(-vehicle.getPosition().yaw()));
        } else if (vehicle.getEntityType().key().value().contains("boat")) {
            double animalOffset = isEntityAnimal(passenger.getEntityType()) ? 0.2 : 0;
            if (CHEST_BOATS.contains(passenger.getEntityType())) {
                // Special case: Single passenger
                if (vehicle.getPassengers().size() == 1) {
                    return new Vec(0, vehicle.getEntityType().height() / 3f, 0.15).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
                } else {
                    if (passengerIndex == 0) {
                        return new Vec(0, vehicle.getEntityType().height() / 3f, 0.15 + animalOffset).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
                    } else {
                        return new Vec(0, vehicle.getEntityType().height() / 3f, -0.6 + animalOffset).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
                    }
                }
            } else {
                // Special case: Single passenger
                if (vehicle.getPassengers().size() == 1) {
                    return new Vec(0, vehicle.getEntityType().height() / 3f, 0).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
                } else {
                    if (passengerIndex == 0) {
                        return new Vec(0, vehicle.getEntityType().height() / 3f, 0.2 + animalOffset).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
                    } else {
                        return new Vec(0, vehicle.getEntityType().height() / 3f, -0.6 + animalOffset).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
                    }
                }
            }

        } else if (vehicle.getEntityType().key().value().contains("raft")) {
            if (passengerIndex == 0) {
                return new Vec(0, vehicle.getEntityType().height() / 0.8888f, 0.2).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
            } else {
                return new Vec(0, vehicle.getEntityType().height() / 0.8888f, -0.6).rotateAroundY(Math.toRadians(vehicle.getPosition().yaw() * -1));
            }
        } else {
            // Passenger position offset is a combination of vehicle passenger position and passenger vehicle position (if it exists)
            List<Double> vehiclePassengerPos = vehicle.getEntityType().registry().entityAttachment("PASSENGER");
            Vec passengerOffset = attachmentListToVec(vehiclePassengerPos, new Vec(0, vehicle.getEntityType().height(), 0));
            List<Double> passengerVehiclePos = passenger.getEntityType().registry().entityAttachment("VEHICLE");
            Vec vehicleOffset = attachmentListToVec(passengerVehiclePos, Vec.ZERO);
            return passengerOffset.sub(vehicleOffset).rotateAroundY(Math.toRadians(-vehicle.getPosition().yaw()));
        }
    }

    private static Vec attachmentListToVec(@Nullable List<Double> attachmentList, Vec fallback) {
        if (attachmentList == null) {
            return fallback;
        }
        if (attachmentList.size() < 3) {
            return fallback;
        }
        return new Vec(attachmentList.get(0), attachmentList.get(1), attachmentList.get(2));
    }

    private static final Set<EntityType> CHEST_BOATS = new HashSet<>() {
        {
            add(EntityType.ACACIA_CHEST_BOAT);
            add(EntityType.BIRCH_CHEST_BOAT);
            add(EntityType.CHERRY_CHEST_BOAT);
            add(EntityType.DARK_OAK_CHEST_BOAT);
            add(EntityType.JUNGLE_CHEST_BOAT);
            add(EntityType.MANGROVE_CHEST_BOAT);
            add(EntityType.OAK_CHEST_BOAT);
            add(EntityType.PALE_OAK_CHEST_BOAT);
            add(EntityType.SPRUCE_CHEST_BOAT);
        }
    };

    private static final Set<EntityType> ANIMALS = new HashSet<>() {
        {
            add(EntityType.ARMADILLO);
            add(EntityType.AXOLOTL);
            add(EntityType.BEE);
            add(EntityType.CAMEL);
            add(EntityType.CAMEL_HUSK);
            add(EntityType.CAT);
            add(EntityType.CHICKEN);
            add(EntityType.COW);
            add(EntityType.DONKEY);
            add(EntityType.FOX);
            add(EntityType.FROG);
            add(EntityType.GOAT);
            add(EntityType.HAPPY_GHAST);
            add(EntityType.HOGLIN);
            add(EntityType.HORSE);
            add(EntityType.LLAMA);
            add(EntityType.MOOSHROOM);
            add(EntityType.MULE);
            add(EntityType.NAUTILUS);
            add(EntityType.OCELOT);
            add(EntityType.PANDA);
            add(EntityType.PARROT);
            add(EntityType.PIG);
            add(EntityType.POLAR_BEAR);
            add(EntityType.RABBIT);
            add(EntityType.SHEEP);
            add(EntityType.SKELETON_HORSE);
            add(EntityType.SNIFFER);
            add(EntityType.STRIDER);
            add(EntityType.TRADER_LLAMA);
            add(EntityType.TURTLE);
            add(EntityType.WOLF);
            add(EntityType.ZOMBIE_HORSE);
            add(EntityType.ZOMBIE_NAUTILUS);
        }
    };

    private static boolean isEntityAnimal(EntityType entityType) {
        return ANIMALS.contains(entityType);
    }

    private EntityUtils() {}
}
