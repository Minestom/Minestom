package net.minestom.server.entity;

import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record EntityTypeImpl(RegistryData.EntityEntry registry) implements EntityType {
    private static final RegistryData.Container<EntityType> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.ENTITIES,
            (namespace, properties) -> new EntityTypeImpl(RegistryData.entity(namespace, properties)));

    static EntityType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static EntityType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
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
