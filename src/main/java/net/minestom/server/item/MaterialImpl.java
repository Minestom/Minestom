package net.minestom.server.item;

import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record MaterialImpl(RegistryData.MaterialEntry registry) implements Material {
    static final Registry<Material> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.ITEM,
            (namespace, properties) -> new MaterialImpl(RegistryData.material(namespace, properties)));

    static @UnknownNullability Material get(RegistryKey<Material> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
