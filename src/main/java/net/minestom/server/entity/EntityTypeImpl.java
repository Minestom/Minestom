package net.minestom.server.entity;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record EntityTypeImpl(Registry.EntityEntry registry) implements EntityType {
    private static final Registry.Container<EntityType> CONTAINER = Registry.createStaticContainer(Registry.Resource.ENTITIES,
            (key, properties) -> new EntityTypeImpl(Registry.entity(key, properties)));

    static EntityType get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static EntityType getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
    }

    static EntityType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<EntityType> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
