package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionTypeImpl(Key namespace, int id) implements PotionType {
    private static final Registry.Container<PotionType> CONTAINER = Registry.createStaticContainer(Registry.Resource.POTION_TYPES,
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
