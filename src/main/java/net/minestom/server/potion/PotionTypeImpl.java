package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record PotionTypeImpl(Key key, int id) implements PotionType {
    static final Registry<PotionType> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.POTION_TYPE,
            (namespace, properties) -> new PotionTypeImpl(namespace.key(), properties.getInt("id")));

    static @UnknownNullability PotionType get(RegistryKey<PotionType> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
