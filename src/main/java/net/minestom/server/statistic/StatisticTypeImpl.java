package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record StatisticTypeImpl(Key key, int id) implements StatisticType {
    private static final Registry.Container<StatisticType> CONTAINER = Registry.createStaticContainer(Registry.Resource.STATISTICS,
            (key, properties) -> new StatisticTypeImpl(Key.key(key), properties.getInt("id")));

    static StatisticType get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static StatisticType getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
    }

    static StatisticType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<StatisticType> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
