package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record PotionTypeImpl(Key key, int id) implements PotionType {
    static final StaticRegistry<PotionType> REGISTRY = RegistryData.createStaticRegistry(
            RegistryData.Resource.POTION_TYPES, "minecraft:potion_type",
            (namespace, properties) -> new PotionTypeImpl(Key.key(namespace), properties.getInt("id")));

    static @UnknownNullability PotionType get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
