package net.minestom.server.item;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record MaterialImpl(RegistryData.MaterialEntry registry) implements Material {
    static final StaticRegistry<Material> REGISTRY = RegistryData.createStaticRegistryWithTags(
            RegistryData.Resource.ITEMS, RegistryData.Resource.ITEM_TAGS, "minecraft:item",
            (namespace, properties) -> new MaterialImpl(RegistryData.material(namespace, properties)));

    static @UnknownNullability Material get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
