package net.minestom.server.entity.villager;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record VillagerTypeImpl(Registry.VillagerType registry, int id) implements VillagerType {
    private static final Registry.Container<VillagerType> CONTAINER = Registry.createStaticContainer(Registry.Resource.VILLAGER_TYPES, VillagerTypeImpl::createImpl);

    public VillagerTypeImpl(Registry.VillagerType registry) {
        this(registry, registry.id());
    }

    private static VillagerType createImpl(String namespace, Registry.Properties properties) {
        return new VillagerTypeImpl(Registry.villagerType(namespace, properties));
    }

    static VillagerType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static VillagerType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static VillagerType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<VillagerType> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
