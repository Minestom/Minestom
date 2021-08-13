package net.minestom.server.potion;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class PotionTypeImpl implements PotionType {
    private static final Registry.Container<PotionType> CONTAINER = new Registry.Container<>(Registry.Resource.POTION_TYPES,
            (loader, namespace, object) -> {
                final int id = object.get("id").getAsInt();
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

    private final NamespaceID namespaceID;
    private final int id;

    PotionTypeImpl(NamespaceID namespaceID, int id) {
        this.namespaceID = namespaceID;
        this.id = id;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return namespaceID;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return name();
    }
}
