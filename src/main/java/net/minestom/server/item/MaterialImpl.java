package net.minestom.server.item;

import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record MaterialImpl(RegistryData.MaterialEntry registry) implements Material {
    private static final RegistryData.Container<Material> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.ITEMS,
            (namespace, properties) -> new MaterialImpl(RegistryData.material(namespace, properties)));

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
