package net.minestom.server.potion;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record PotionTypeImpl(NamespaceID namespace, int id) implements PotionType {
    private static final Registry.Container<PotionType> CONTAINER = Registry.createStaticContainer(Registry.Resource.POTION_TYPES, PotionTypeImpl::createImpl);

    private static PotionType createImpl(String namespace, Registry.Properties properties) {
        return new PotionTypeImpl(NamespaceID.from(namespace), properties.getInt("id"));
    }

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
