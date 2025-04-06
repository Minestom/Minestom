package net.minestom.server.potion;

import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionEffectImpl(RegistryData.PotionEffectEntry registry) implements PotionEffect {
    private static final RegistryData.Container<PotionEffect> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.POTION_EFFECTS,
            (namespace, properties) -> new PotionEffectImpl(RegistryData.potionEffect(namespace, properties)));

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
