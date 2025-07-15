package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

record PotionTypeImpl(Key key, int id) implements PotionType {
    static final Registry<PotionType> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:potion_type"),
            (namespace, properties) -> new PotionTypeImpl(Key.key(namespace), properties.getInt("id")));

    static @UnknownNullability PotionType get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
