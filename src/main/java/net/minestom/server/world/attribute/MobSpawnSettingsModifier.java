package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.utils.WeightedList;
import net.minestom.server.world.attribute.MobSpawnSettings.MobCategory;
import net.minestom.server.world.attribute.MobSpawnSettings.SpawnerData;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Overlays the argument onto the inherited settings. Categories defined by the argument replace the
 * inherited ones, other categories are kept, and spawn costs from both are merged with the argument winning.
 */
record MobSpawnSettingsModifier() implements EnvironmentAttribute.Modifier<MobSpawnSettings, MobSpawnSettings> {
    static final MobSpawnSettingsModifier OVERLAY = new MobSpawnSettingsModifier();

    @java.lang.Override
    public MobSpawnSettings modify(MobSpawnSettings subject, MobSpawnSettings argument) {
        if (subject.spawnsByCategory().isEmpty() && subject.spawnCosts().isEmpty()) return argument;
        if (argument.spawnsByCategory().isEmpty() && argument.spawnCosts().isEmpty()) return subject;
        if (argument.spawnsByCategory().keySet().containsAll(subject.spawnsByCategory().keySet())
                && argument.spawnCosts().keySet().containsAll(subject.spawnCosts().keySet())) {
            return argument;
        }
        final Map<MobCategory, WeightedList<SpawnerData>> spawns = new EnumMap<>(subject.spawnsByCategory());
        spawns.putAll(argument.spawnsByCategory());
        final var costs = new HashMap<>(subject.spawnCosts());
        costs.putAll(argument.spawnCosts());
        return new MobSpawnSettings(spawns, costs);
    }

    @java.lang.Override
    public Codec<MobSpawnSettings> argumentCodec() {
        return MobSpawnSettings.CODEC;
    }
}
