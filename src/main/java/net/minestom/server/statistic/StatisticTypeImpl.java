package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

record StatisticTypeImpl(Key key, int id) implements StatisticType {
    static final Registry<StatisticType> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.CUSTOM_STATISTICS,
            (namespace, properties) -> new StatisticTypeImpl(namespace.key(), properties.getInt("id")));

    static StatisticType get(@NotNull RegistryKey<StatisticType> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
