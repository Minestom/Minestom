package net.minestom.server.entity;

import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record EntityTypeImpl(RegistryData.EntityEntry registry) implements EntityType {
    static final Registry<EntityType> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.ENTITY_TYPE,
            (namespace, properties) -> new EntityTypeImpl(RegistryData.entity(namespace, properties)));

    static @UnknownNullability EntityType get(RegistryKey<EntityType> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
