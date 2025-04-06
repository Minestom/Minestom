package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionTypeImpl(Key key, int id) implements PotionType {
    private static final RegistryData.Container<PotionType> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.POTION_TYPES,
            (namespace, properties) -> new PotionTypeImpl(Key.key(namespace), properties.getInt("id")));

    static PotionType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static PotionType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static PotionType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<PotionType> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
