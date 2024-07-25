package net.minestom.server.potion;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionEffectImpl(Registry.PotionEffectEntry registry) implements PotionEffect {
    private static final Registry.Container<PotionEffect> CONTAINER = Registry.createStaticContainer(Registry.Resource.POTION_EFFECTS,
            (key, properties) -> new PotionEffectImpl(Registry.potionEffect(key, properties)));

    static PotionEffect get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static PotionEffect getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
    }

    static PotionEffect getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<PotionEffect> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
