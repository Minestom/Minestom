package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.monster.PiglinMeta;
import net.minestom.server.entity.metadata.monster.ZoglinMeta;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class EntityUtils {
    private static final MountInfo DEFAULT_MOUNT_INFO = MountInfo.heightOnly(height -> height, 0, true);
    private static final MountInfo MINECART_MOUNT_INFO = MountInfo.heightOnly(height -> 0.1875);
    private static final MountInfo SLIME_MOUNT_INFO = new MountInfo(slime ->
            new Vec(0, slime.getBoundingBox().height() - 0.015625 * ((SlimeMeta)slime.getEntityMeta()).getSize(), 0));
    private static final MountInfo LLAMA_MOUNT_INFO = new MountInfo(llama -> llama.getPosition()
            .direction().withY(0).normalize().mul(-0.3 * (isBaby(llama) ? 0.5 : 1))
            .add(0, llama.getEntityType().height() - (isBaby(llama) ? 0.8125 : 0.5), 0));
    private static final Map<EntityType, MountInfo> MOUNT_INFO = Map.<EntityType, MountInfo>ofEntries(
            Map.entry(EntityType.SKELETON,         MountInfo.heightOnly(height -> height, -0.7)),
            Map.entry(EntityType.STRAY,            MountInfo.heightOnly(height -> height, -0.7)),
            Map.entry(EntityType.WITHER_SKELETON,  MountInfo.heightOnly(height -> height, -0.875)),
            Map.entry(EntityType.ALLAY,            MountInfo.heightOnly(height -> height,  0.04)),
            Map.entry(EntityType.PLAYER,           MountInfo.heightOnly(height -> height, -0.6)),
            Map.entry(EntityType.GIANT,            MountInfo.heightOnly(height -> height, -3.75)),

            Map.entry(EntityType.CAT,              MountInfo.heightOnly(height -> height - 0.1875)),
            Map.entry(EntityType.GOAT,             MountInfo.heightOnly(height -> height - 0.1875)),

            Map.entry(EntityType.PIG,              MountInfo.heightOnly(height -> height - 0.03125)),
            Map.entry(EntityType.COW,              MountInfo.heightOnly(height -> height - 0.03125)),
            Map.entry(EntityType.WOLF,             MountInfo.heightOnly(height -> height - 0.03125)),
            Map.entry(EntityType.WITCH,            MountInfo.heightOnly(height -> height - 0.03125)),

            Map.entry(EntityType.FOX,              MountInfo.heightOnly(height -> height - 0.0625)),
            Map.entry(EntityType.OCELOT,           MountInfo.heightOnly(height -> height - 0.0625)),
            Map.entry(EntityType.SHEEP,            MountInfo.heightOnly(height -> height - 0.0625)),
            Map.entry(EntityType.ENDERMITE,        MountInfo.heightOnly(height -> height - 0.0625)),
            Map.entry(EntityType.SILVERFISH,       MountInfo.heightOnly(height -> height - 0.0625)),
            Map.entry(EntityType.VEX,              MountInfo.heightOnly(height -> height - 0.0625, 0.04)),

            Map.entry(EntityType.RAVAGER,          MountInfo.heightOnly(height -> height + 0.0625, -0.7)),
            Map.entry(EntityType.PIGLIN,           MountInfo.heightOnly(height -> height + 0.0625, -0.7)),
            Map.entry(EntityType.PIGLIN_BRUTE,     MountInfo.heightOnly(height -> height + 0.0625, -0.7)),
            Map.entry(EntityType.ZOMBIE,           MountInfo.heightOnly(height -> height + 0.0625, -0.7)),
            Map.entry(EntityType.DROWNED,          MountInfo.heightOnly(height -> height + 0.0625, -0.7)),

            Map.entry(EntityType.GHAST,            MountInfo.heightOnly(height -> height + 0.0625, 0.5)),

            Map.entry(EntityType.ZOMBIFIED_PIGLIN, MountInfo.heightOnly(height -> height + 0.05,  -0.7)),

            Map.entry(EntityType.PILLAGER,         MountInfo.heightOnly(height -> height + 0.05, -0.6)),
            Map.entry(EntityType.EVOKER,           MountInfo.heightOnly(height -> height + 0.05, -0.6)),
            Map.entry(EntityType.VINDICATOR,       MountInfo.heightOnly(height -> height + 0.05, -0.6)),
            Map.entry(EntityType.ILLUSIONER,       MountInfo.heightOnly(height -> height + 0.05, -0.6)),

            Map.entry(EntityType.ENDERMAN,         MountInfo.heightOnly(height -> height - 0.09375)),
            Map.entry(EntityType.PARROT,           MountInfo.heightOnly(height -> height - 0.4375f)),
            Map.entry(EntityType.SNIFFER,          MountInfo.heightOnly(height -> height + 0.34375)),
            Map.entry(EntityType.ELDER_GUARDIAN,   MountInfo.heightOnly(height -> height + 0.353215)),
            Map.entry(EntityType.GUARDIAN,         MountInfo.heightOnly(height -> height + 0.125)),
            Map.entry(EntityType.HUSK,             MountInfo.heightOnly(height -> height + 0.125)),
            Map.entry(EntityType.FROG,             MountInfo.heightOnly(height -> height - 0.125f)),
            Map.entry(EntityType.WARDEN,           MountInfo.heightOnly(height -> height + 0.25)),
            Map.entry(EntityType.ZOMBIE_VILLAGER,  MountInfo.heightOnly(height -> height + 0.175)),

            Map.entry(EntityType.SPIDER,           MountInfo.heightOnly(height -> height * 0.85,  -0.3125, true)),
            Map.entry(EntityType.CAVE_SPIDER,      MountInfo.heightOnly(height -> height * 0.85,  -0.21875, true)),
            Map.entry(EntityType.PHANTOM,          MountInfo.heightOnly(height -> height * 0.675, -0.125, true)),

            Map.entry(EntityType.HOGLIN,           MountInfo.heightOnly(height -> height - 0.09375)),
            Map.entry(EntityType.ZOGLIN,           MountInfo.heightOnly(height -> height - 0.09375)),

            Map.entry(EntityType.PANDA,            MountInfo.explicitBabyHeight((height, isBaby) -> height - (isBaby ? 0.4375 : 0))),
            Map.entry(EntityType.SKELETON_HORSE,   MountInfo.explicitBabyHeight((height, isBaby) -> height - (isBaby ? 0.03125 : 0.28125))),


            Map.entry(EntityType.TURTLE,           new MountInfo(turtle -> turtle.getPosition()
                    .direction().withY(0).normalize().mul(-0.25 * (isBaby(turtle) ? 0.3 : 1))
                    .add(0, turtle.getBoundingBox().height() + (isBaby(turtle) ? 0 : 0.15625), 0), 0, 0.3, false)),
            Map.entry(EntityType.CHICKEN,          new MountInfo(chicken -> chicken.getPosition()
                    .direction().withY(0).normalize().mul(-0.1 * (isBaby(chicken) ? 0.5 : 1))
                    .add(0, chicken.getBoundingBox().height(), 0))),

            Map.entry(EntityType.SLIME,            SLIME_MOUNT_INFO),
            Map.entry(EntityType.MAGMA_CUBE,       SLIME_MOUNT_INFO),

            Map.entry(EntityType.MINECART,         MINECART_MOUNT_INFO),
            Map.entry(EntityType.CHEST_MINECART,   MINECART_MOUNT_INFO),
            Map.entry(EntityType.FURNACE_MINECART, MINECART_MOUNT_INFO),
            Map.entry(EntityType.HOPPER_MINECART,  MINECART_MOUNT_INFO),
            Map.entry(EntityType.TNT_MINECART,     MINECART_MOUNT_INFO),

            Map.entry(EntityType.LLAMA,            LLAMA_MOUNT_INFO),
            Map.entry(EntityType.TRADER_LLAMA,     LLAMA_MOUNT_INFO),

            Map.entry(EntityType.BOAT,             MountInfo.heightOnly(height -> height / 3, 0, true)),
            Map.entry(EntityType.CHEST_BOAT,       MountInfo.heightOnly(height -> height / 3, 0, true))
    );

    /**
     * Get the offset of a passenger based on this specific entity vehicle-passenger combination
     *
     * @param vehicle the vehicle to use
     * @param passenger the passenger to be offset
     * @return a vec to offset the passenger by
     */
    public static @NotNull Vec getPassengerOffset(@NotNull Entity vehicle, @NotNull Entity passenger) {
        MountInfo vehicleMountInfo = MOUNT_INFO.getOrDefault(passenger.getEntityType(), DEFAULT_MOUNT_INFO);
        MountInfo passengerMountInfo = MOUNT_INFO.getOrDefault(passenger.getEntityType(), DEFAULT_MOUNT_INFO);

        Vec vehicleOffset = vehicleMountInfo.vehicleOffset().apply(vehicle);
        if (!vehicleMountInfo.ignoreVehicleBabyScaling() && isBaby(vehicle)) {
            // The extra mount height should be affected by the babyScaling multiplier
            double height = vehicle.getBoundingBox().height();
            double difference = height - vehicleOffset.y();
            vehicleOffset = new Vec(0, height + difference * vehicleMountInfo.babyScaling(), 0);
        }

        double passengerOffset = passengerMountInfo.passengerHeightOffset();
        if (isBaby(passenger)) {
            passengerOffset *= passengerMountInfo.babyScaling();
        }

        return vehicleOffset.add(0, passengerOffset, 0);
    }

    /**
     * Check the entity's metadata to determine if it is a baby.
     *
     * @param entity the entity to check
     * @return true if the entity is baby, otherwise false
     */
    public static boolean isBaby(@NotNull Entity entity) {
        EntityMeta entityMeta = entity.getEntityMeta();
        if (entityMeta instanceof AgeableMobMeta meta) return meta.isBaby();
        if (entityMeta instanceof ZombieMeta meta) return meta.isBaby();
        if (entityMeta instanceof PiglinMeta meta) return meta.isBaby();
        if (entityMeta instanceof ZoglinMeta meta) return meta.isBaby();
        return false;
    }

    private EntityUtils() {
    }

    public static boolean isOnGround(@NotNull Entity entity) {
        final Chunk chunk = entity.getChunk();
        if (chunk == null)
            return false;
        final Pos entityPosition = entity.getPosition();
        // TODO: check entire bounding box
        try {
            final Block block;
            synchronized (chunk) {
                block = chunk.getBlock(entityPosition.sub(0, 1, 0));
            }
            return block.isSolid();
        } catch (NullPointerException e) {
            // Probably an entity at the border of an unloaded chunk
            return false;
        }
    }

    /**
     *  A representation of the unique mount properties each entity has
     *
     * @param vehicleOffset the offset this entity displaces its passengers by
     * @param passengerHeightOffset the offset this entity has when it is a passenger
     * @param babyScaling how much to automatically multiply height values by when an entity is a baby
     * @param ignoreVehicleBabyScaling true if the vehicle height should not be multiplied by babyScaling
     */
    private record MountInfo(Function<Entity, Vec> vehicleOffset, double passengerHeightOffset, double babyScaling,
                             boolean ignoreVehicleBabyScaling) {
        private MountInfo(Function<Entity, Vec> mountOffset, double passengerHeightOffset) {
            this(mountOffset, passengerHeightOffset, 0.5, false);
        }

        private MountInfo(Function<Entity, Vec> mountOffset) {
            this(mountOffset, 0);
        }

        /**
         * Shorthand for when we only need the height of an entity
         */
        private static MountInfo heightOnly(UnaryOperator<Double> height, double passengerOffset, boolean ignoreVehicleBabyScaling) {
            return new MountInfo(entity -> new Vec(0, height.apply(entity.getBoundingBox().height()), 0),
                    passengerOffset, 0.5, ignoreVehicleBabyScaling);
        }

        private static MountInfo heightOnly(UnaryOperator<Double> height, double passengerOffset) {
            return new MountInfo(entity -> new Vec(0, height.apply(entity.getBoundingBox().height()), 0), passengerOffset);
        }

        private static MountInfo heightOnly(UnaryOperator<Double> height) {
            return heightOnly(height, 0);
        }

        /**
         * Shorthand for when unique height + baby size logic is required
         */
        private static MountInfo explicitBabyHeight(BiFunction<Double, Boolean, Double> babyHeight) {
            return new MountInfo(entity -> new Vec(0, babyHeight.apply(entity.getBoundingBox().height(), isBaby(entity)), 0), 0);
        }
    }
}
