package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;

record StatisticTypeImpl(Key key, int id) implements StatisticType {
    static final Registry<StatisticType> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:statistic_type"),
            (namespace, properties) -> new StatisticTypeImpl(Key.key(namespace), properties.getInt("id")));

    static StatisticType get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
