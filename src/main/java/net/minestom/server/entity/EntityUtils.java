package net.minestom.server.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.AgeableMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.entity.metadata.water.fish.PufferfishMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class EntityUtils {
    private static final BiFunction<Entity, Entity, Vec>[] PASSENGER_ATTACHMENTS = new BiFunction[EntityType.values().size()];
    private static final Function<Entity, Vec>[] VEHICLE_ATTACHMENTS = new Function[EntityType.values().size()];

    static {
        // Zombies
        PASSENGER_ATTACHMENTS[EntityType.ZOMBIE_VILLAGER.id()] = EntityUtils::zombieExtendedPassengerAttachment;
        VEHICLE_ATTACHMENTS[EntityType.ZOMBIE_VILLAGER.id()]   = EntityUtils::ageableVehicleAttachment;
        PASSENGER_ATTACHMENTS[EntityType.HUSK.id()]            = EntityUtils::zombieExtendedPassengerAttachment;
        VEHICLE_ATTACHMENTS[EntityType.HUSK.id()]              = EntityUtils::ageableVehicleAttachment;

        // Spiders
        PASSENGER_ATTACHMENTS[EntityType.SPIDER.id()]      = (v, p) -> EntityUtils.spiderPassengerAttachment(v, p, 0.3125);
        PASSENGER_ATTACHMENTS[EntityType.CAVE_SPIDER.id()] = (v, p) -> EntityUtils.spiderPassengerAttachment(v, p, 0.21875);

        // Slimes
        PASSENGER_ATTACHMENTS[EntityType.SLIME.id()]      = EntityUtils::slimePassengerAttachment;
        PASSENGER_ATTACHMENTS[EntityType.MAGMA_CUBE.id()] = EntityUtils::slimePassengerAttachment;

        // Horses
        PASSENGER_ATTACHMENTS[EntityType.HORSE.id()]          = (v, p) -> EntityUtils.horsePassengerAttachment(v, p, 0.125);
        PASSENGER_ATTACHMENTS[EntityType.DONKEY.id()]         = (v, p) -> EntityUtils.horsePassengerAttachment(v, p, -0.15625);
        PASSENGER_ATTACHMENTS[EntityType.MULE.id()]           = (v, p) -> EntityUtils.horsePassengerAttachment(v, p, -0.15625);
        PASSENGER_ATTACHMENTS[EntityType.ZOMBIE_HORSE.id()]   = (v, p) -> EntityUtils.horsePassengerAttachment(v, p, -0.03125);
        PASSENGER_ATTACHMENTS[EntityType.SKELETON_HORSE.id()] = (v, p) -> EntityUtils.horsePassengerAttachment(v, p, -0.03125);
        PASSENGER_ATTACHMENTS[EntityType.LLAMA.id()]          = (v, p) -> EntityUtils.horsePassengerAttachment(v, p, -0.8125);

        // Other
        PASSENGER_ATTACHMENTS[EntityType.PANDA.id()]       = EntityUtils::pandaPassengerAttachment;
        PASSENGER_ATTACHMENTS[EntityType.PUFFERFISH.id()]  = EntityUtils::pufferFishPassengerAttachment;
        PASSENGER_ATTACHMENTS[EntityType.INTERACTION.id()] = EntityUtils::interactionPassengerAttachment;

        // Unique ageable
        PASSENGER_ATTACHMENTS[EntityType.ARMADILLO.id()] = (v, p) -> EntityUtils.ageablePassengerAttachment(v, p, 0.6);
        PASSENGER_ATTACHMENTS[EntityType.TURTLE.id()]    = EntityUtils::turtlePassengerAttachment;

        // Default ageable
        Set<EntityType> defaultAgeableTypes = Set.of(EntityType.AXOLOTL, EntityType.BEE, EntityType.CAT, EntityType.CHICKEN, EntityType.COW, EntityType.DROWNED,
                EntityType.FOX, EntityType.GOAT, EntityType.HOGLIN, EntityType.MOOSHROOM, EntityType.OCELOT, EntityType.PIG, EntityType.PIGLIN, EntityType.POLAR_BEAR,
                EntityType.RABBIT, EntityType.SHEEP, EntityType.SNIFFER, EntityType.STRIDER, EntityType.VILLAGER, EntityType.WANDERING_TRADER, EntityType.WOLF,
                EntityType.ZOGLIN, EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN);
        for (int i = 0; i < EntityType.values().size(); i++) {
            EntityType type = EntityType.fromId(i);
            if (defaultAgeableTypes.contains(type)) {
                PASSENGER_ATTACHMENTS[i] = (v, p) -> EntityUtils.ageablePassengerAttachment(v, p, 0.5);
                VEHICLE_ATTACHMENTS[i] = EntityUtils::ageableVehicleAttachment;
                continue;
            }

            // Fill in defaults
            if (PASSENGER_ATTACHMENTS[i] == null) {
                PASSENGER_ATTACHMENTS[i] = EntityUtils::defaultPassengerAttachment;
            }
            if (VEHICLE_ATTACHMENTS[i] == null) {
                VEHICLE_ATTACHMENTS[i] = EntityUtils::defaultVehicleAttachment;
            }
        }
    }

    /**
     * Return the passenger attachment offset for the passenger riding this vehicle.
     * @param vehicle the vehicle of the passenger
     * @param passenger the passenger on the vehicle
     * @return the passenger attachment offset
     */
    public static @NotNull Vec getPassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        return PASSENGER_ATTACHMENTS[vehicle.getEntityType().id()].apply(vehicle, passenger);
    }

    /**
     * Return the vehicle attachment offset for this passenger.
     * @param passenger the passenger on the vehicle
     * @return the vehicle attachment offset
     */
    public static @NotNull Vec getVehicleAttachment(@NotNull Entity passenger) {
        return VEHICLE_ATTACHMENTS[passenger.getEntityType().id()].apply(passenger);
    }

    private static @NotNull Vec defaultPassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        Vec passengerAttachment = vehicle.getEntityType().registry().passengerAttachment();
        if (passengerAttachment.z() != 0) {
            // Some vehicles have an additional horizontal passenger offset indicated on the attachment z axis
            passengerAttachment = passengerAttachment.rotateAroundY(-Math.toRadians(vehicle.getPosition().yaw()));
        }

        return passengerAttachment;
    }

    private static @NotNull Vec ageablePassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger, double scale) {
        Vec attachment = defaultPassengerAttachment(vehicle, passenger);
        return ((AgeableMeta) vehicle.getEntityMeta()).isBaby() ? attachment.mul(scale) : attachment;
    }

    private static @NotNull Vec horsePassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger, double babyOffset) {
        boolean isBaby = ((AgeableMeta) vehicle.getEntityMeta()).isBaby();
        if (isBaby) {
            double zOffset = vehicle.getEntityType().registry().passengerAttachment().z() * 0.5;
            Vec attachment = new Vec(0, (vehicle.getEntityType().registry().height() + babyOffset) * 0.5, zOffset);
            if (attachment.z() == 0) return attachment;
            return attachment.rotateAroundY(-Math.toRadians(vehicle.getPosition().yaw()));
        }
        return defaultPassengerAttachment(vehicle, passenger);
    }

    private static @NotNull Vec turtlePassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        boolean isBaby = ((AgeableMeta) vehicle.getEntityMeta()).isBaby();
        if (isBaby) return new Vec(0, vehicle.getEntityType().height() * 0.3, -0.25 * 0.3);
        return defaultPassengerAttachment(vehicle, passenger);
    }

    private static @NotNull Vec pandaPassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        boolean isBaby = ((AgeableMeta) vehicle.getEntityMeta()).isBaby();
        if (isBaby) return new Vec(0, 0.40625, 0);
        return defaultPassengerAttachment(vehicle, passenger);
    }

    private static @NotNull Vec zombieExtendedPassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        boolean isBaby = ((AgeableMeta) vehicle.getEntityMeta()).isBaby();
        if (isBaby) return EntityType.ZOMBIE.registry().passengerAttachment().mul(0.5);
        return defaultPassengerAttachment(vehicle, passenger);
    }

    private static @NotNull Vec spiderPassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger, double widerAttachmentHeight) {
        Vec attachment = defaultPassengerAttachment(vehicle, passenger);
        if (passenger.getBoundingBox().width() <= vehicle.getBoundingBox().width()) {
            return attachment.sub(0, widerAttachmentHeight, 0);
        }
        return attachment;
    }

    private static @NotNull Vec slimePassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        return new Vec(0, vehicle.getBoundingBox().height() - 0.015625 * ((SlimeMeta) vehicle.getEntityMeta()).getSize(), 0);
    }

    private static @NotNull Vec pufferFishPassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        double height = switch(((PufferfishMeta) vehicle.getEntityMeta()).getState()) {
            case UNPUFFED -> 0.35;
            case SEMI_PUFFED -> 0.5;
            case FULLY_PUFFED -> 0.7;
        };
        return new Vec(0, height, 0);
    }

    private static @NotNull Vec interactionPassengerAttachment(@NotNull Entity vehicle, @NotNull Entity passenger) {
        // Interaction entities only have a passenger height if pose metadata has been explicitly set
        boolean setPose = vehicle.metadata.getIndex(6, null) != null;
        return new Vec(0, setPose ? ((InteractionMeta) vehicle.getEntityMeta()).getHeight() : 0, 0);
    }

    private static @NotNull Vec defaultVehicleAttachment(@NotNull Entity vehicle) {
        return vehicle.getEntityType().registry().vehicleAttachment();
    }

    private static @NotNull Vec ageableVehicleAttachment(@NotNull Entity vehicle) {
        Vec attachment = defaultVehicleAttachment(vehicle);
        return ((AgeableMeta) vehicle.getEntityMeta()).isBaby() ? attachment.mul(0.5) : attachment;
    }

    private EntityUtils() {
    }
}
