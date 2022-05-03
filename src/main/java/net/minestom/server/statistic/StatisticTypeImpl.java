package net.minestom.server.statistic;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record StatisticTypeImpl(NamespaceID namespace, int id) implements StatisticType {
    private static final Registry.Container<StatisticType> CONTAINER = Registry.createContainer(Registry.Resource.STATISTICS,
            (namespace, properties) -> new StatisticTypeImpl(NamespaceID.from(namespace), properties.getInt("id")));

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
