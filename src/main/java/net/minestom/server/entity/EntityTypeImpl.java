package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record EntityTypeImpl(RegistryData.EntityEntry registry) implements EntityType {
    static final Registry<EntityType> REGISTRY = RegistryData.createStaticRegistry(Key.key("entity_type"),
            (namespace, properties) -> new EntityTypeImpl(RegistryData.entity(namespace, properties)));

    static @UnknownNullability EntityType get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
