package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

record PotionEffectImpl(RegistryData.PotionEffectEntry registry) implements PotionEffect {
    static final Registry<PotionEffect> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:potion_effect"),
            (namespace, properties) -> new PotionEffectImpl(RegistryData.potionEffect(namespace, properties)));

    static @UnknownNullability PotionEffect get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
