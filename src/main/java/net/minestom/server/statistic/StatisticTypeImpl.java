package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record StatisticTypeImpl(Key namespace, int id) implements StatisticType {
    private static final Registry.Container<StatisticType> CONTAINER = Registry.createStaticContainer(Registry.Resource.STATISTICS,
            (namespace, properties) -> new StatisticTypeImpl(Key.key(namespace), properties.getInt("id")));

    static StatisticType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static StatisticType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
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
