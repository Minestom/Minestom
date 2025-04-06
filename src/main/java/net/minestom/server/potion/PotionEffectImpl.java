package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record PotionEffectImpl(RegistryData.PotionEffectEntry registry) implements PotionEffect {
    static final StaticRegistry<PotionEffect> REGISTRY = RegistryData.createStaticRegistry(
            RegistryData.Resource.POTION_EFFECTS, "minecraft:potion_effect",
            (namespace, properties) -> new PotionEffectImpl(RegistryData.potionEffect(namespace, properties)));

    static @UnknownNullability PotionEffect get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
