package net.minestom.server.item;

import net.minestom.server.registry.StaticRegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record MaterialImpl(StaticRegistryData.MaterialEntry registry) implements Material {
    private static final StaticRegistryData.Container<Material> CONTAINER = StaticRegistryData.createStaticContainer(StaticRegistryData.Resource.ITEMS,
            (namespace, properties) -> new MaterialImpl(StaticRegistryData.material(namespace, properties)));

    static Material get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Material getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Material getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Material> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
