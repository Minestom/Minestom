package net.minestom.server.potion;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionTypeImpl(NamespaceID namespace, int id) implements PotionType {
    private static final Registry.Container<PotionType> CONTAINER = new Registry.Container<>(Registry.Resource.POTION_TYPES,
            (loader, namespace, object) -> {
                final int id = ((Number) object.get("id")).intValue();
                loader.register(new PotionTypeImpl(NamespaceID.from(namespace), id));
            });

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
