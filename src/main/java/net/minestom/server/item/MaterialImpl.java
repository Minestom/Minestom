package net.minestom.server.item;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

record MaterialImpl(RegistryData.MaterialEntry registry) implements Material {
    static final Registry<Material> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:item"),
            (namespace, properties) -> new MaterialImpl(RegistryData.material(namespace, properties)));

    static @UnknownNullability Material get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
