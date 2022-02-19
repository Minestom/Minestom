package net.minestom.server.map;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record MapColorImpl(Registry.MapColorEntry registry) implements MapColor {
    private static final Registry.Container<MapColor> CONTAINER = Registry.createContainer(Registry.Resource.MAP_COLORS,
            (namespace, properties) -> new MapColorImpl(Registry.mapColor(namespace, properties)));

    static MapColor get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static MapColor getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static MapColor getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<MapColor> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
