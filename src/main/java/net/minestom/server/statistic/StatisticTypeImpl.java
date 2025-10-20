package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record StatisticTypeImpl(Key key, int id) implements StatisticType {
    static final Registry<StatisticType> REGISTRY = RegistryData.createStaticRegistry(Key.key("custom_statistics"),
            (namespace, properties) -> new StatisticTypeImpl(Key.key(namespace), properties.getInt("id")));

    static @UnknownNullability StatisticType get(RegistryKey<StatisticType> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
