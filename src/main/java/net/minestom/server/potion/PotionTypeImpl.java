package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionTypeImpl(Key key, int id) implements PotionType {
    private static final Registry.Container<PotionType> CONTAINER = Registry.createStaticContainer(Registry.Resource.POTION_TYPES,
            (key, properties) -> new PotionTypeImpl(Key.key(key), properties.getInt("id")));

    static PotionType get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static PotionType getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
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
