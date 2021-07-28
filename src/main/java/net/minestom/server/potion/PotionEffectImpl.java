package net.minestom.server.potion;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

final class PotionEffectImpl implements PotionEffect {
    private final Registry.PotionEffectEntry registry;

    PotionEffectImpl(Registry.PotionEffectEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.PotionEffectEntry registry() {
        return registry;
    }
}
