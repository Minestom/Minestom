package net.minestom.server.item;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record MaterialImpl(Registry.MaterialEntry registry) implements Material {
    private static final Registry.Container<Material> CONTAINER = Registry.createStaticContainer(Registry.Resource.ITEMS,
            (key, properties) -> new MaterialImpl(Registry.material(key, properties)));

    static Material get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static Material getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
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
