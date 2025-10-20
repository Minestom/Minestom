package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record EntityTypeImpl(RegistryData.EntityEntry registry) implements EntityType {
    static final Registry<EntityType> REGISTRY = RegistryData.createStaticRegistry(Key.key("entity_type"),
            (namespace, properties) -> new EntityTypeImpl(RegistryData.entity(namespace, properties)));

    static @UnknownNullability EntityType get(RegistryKey<EntityType> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
