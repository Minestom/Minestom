package net.minestom.server.entity;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

final class EntityTypeImpl implements EntityType {
    private final Registry.EntityEntry registry;

    EntityTypeImpl(Registry.EntityEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.EntityEntry registry() {
        return registry;
    }
}
