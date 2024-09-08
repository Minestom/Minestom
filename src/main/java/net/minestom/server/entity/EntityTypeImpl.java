package net.minestom.server.entity;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record EntityTypeImpl(Registry.EntityEntry registry) implements EntityType {
    private static final Registry.Container<EntityType> CONTAINER = Registry.createStaticContainer(
            Registry.loadRegistry(Registry.Resource.ENTITIES, Registry.EntityEntry::new).stream().<EntityType>map(EntityTypeImpl::new).toList());

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
