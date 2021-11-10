package net.minestom.server.potion;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionEffectImpl(Registry.PotionEffectEntry registry) implements PotionEffect {
    private static final Registry.Container<PotionEffect> CONTAINER = new Registry.Container<>(Registry.Resource.POTION_EFFECTS,
            (container, namespace, object) -> container.register(new PotionEffectImpl(Registry.potionEffect(namespace, object, null))));

    static PotionEffect get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static PotionEffect getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
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
