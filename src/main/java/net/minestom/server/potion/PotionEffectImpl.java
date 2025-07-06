package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record PotionEffectImpl(RegistryData.PotionEffectEntry registry) implements PotionEffect {
    static final Registry<PotionEffect> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.POTION_EFFECT,
            (namespace, properties) -> new PotionEffectImpl(RegistryData.potionEffect(namespace, properties)));

    static @UnknownNullability PotionEffect get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    static @UnknownNullability PotionEffect get(@NotNull RegistryKey<PotionEffect> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
