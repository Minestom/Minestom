package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticRegistry;
import org.jetbrains.annotations.NotNull;

record StatisticTypeImpl(Key key, int id) implements StatisticType {
    static final StaticRegistry<StatisticType> REGISTRY = RegistryData.createStaticRegistry(
            RegistryData.Resource.STATISTICS, "minecraft:statistic_type",
            (namespace, properties) -> new StatisticTypeImpl(Key.key(namespace), properties.getInt("id")));

    static StatisticType get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
