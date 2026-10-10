package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.IntProvider;
import net.minestom.server.utils.WeightedList;

import java.util.Map;

/**
 * Natural mob spawns for a location. A category missing from {@link #spawnsByCategory()} is left
 * to the inherited value, while a category mapped to an empty list disables spawns for it.
 *
 * @param spawnsByCategory the weighted spawn entries per mob category
 * @param spawnCosts       the spawn charge and energy budget per entity type
 */
public record MobSpawnSettings(
        Map<MobCategory, WeightedList<SpawnerData>> spawnsByCategory,
        Map<EntityType, MobSpawnCost> spawnCosts
) {
    public static final MobSpawnSettings EMPTY = new MobSpawnSettings(Map.of(), Map.of());

    public static final Codec<MobSpawnSettings> CODEC = StructCodec.struct(
            "spawns_by_category", MobCategory.CODEC.mapValue(WeightedList.codec(SpawnerData.CODEC)), MobSpawnSettings::spawnsByCategory,
            "spawn_costs", EntityType.CODEC.mapValue(MobSpawnCost.CODEC), MobSpawnSettings::spawnCosts,
            MobSpawnSettings::new);

    public MobSpawnSettings {
        spawnsByCategory = Map.copyOf(spawnsByCategory);
        spawnCosts = Map.copyOf(spawnCosts);
    }

    /**
     * Mob categories that share a spawn cap.
     */
    public enum MobCategory {
        MONSTER,
        CREATURE,
        AMBIENT,
        AXOLOTLS,
        UNDERGROUND_WATER_CREATURE,
        WATER_CREATURE,
        WATER_AMBIENT,
        MISC;

        public static final Codec<MobCategory> CODEC = Codec.Enum(MobCategory.class);
    }

    /**
     * One weighted spawn entry.
     *
     * @param type  the entity type to spawn
     * @param count the group size
     */
    public record SpawnerData(EntityType type, IntProvider count) {
        public static final StructCodec<SpawnerData> CODEC = StructCodec.struct(
                "type", EntityType.CODEC, SpawnerData::type,
                "count", IntProvider.CODEC, SpawnerData::count,
                SpawnerData::new);
    }

    /**
     * Spawn cost of an entity type.
     *
     * @param energyBudget the total charge the surroundings may hold before spawning stops
     * @param charge       the charge each spawned entity adds
     */
    public record MobSpawnCost(double energyBudget, double charge) {
        public static final Codec<MobSpawnCost> CODEC = StructCodec.struct(
                "energy_budget", Codec.DOUBLE, MobSpawnCost::energyBudget,
                "charge", Codec.DOUBLE, MobSpawnCost::charge,
                MobSpawnCost::new);
    }
}
