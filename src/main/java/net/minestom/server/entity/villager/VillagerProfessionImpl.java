package net.minestom.server.entity.villager;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record VillagerProfessionImpl(Registry.VillagerProfession registry, int id) implements VillagerProfession {
    private static final Registry.Container<VillagerProfession> CONTAINER = Registry.createStaticContainer(Registry.Resource.VILLAGER_PROFESSION, VillagerProfessionImpl::createImpl);

    public VillagerProfessionImpl(Registry.VillagerProfession registry) {
        this(registry, registry.id());
    }

    private static VillagerProfession createImpl(String namespace, Registry.Properties properties) {
        return new VillagerProfessionImpl(Registry.villagerProfession(namespace, properties));
    }

    static VillagerProfession get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static VillagerProfession getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static VillagerProfession getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<VillagerProfession> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
