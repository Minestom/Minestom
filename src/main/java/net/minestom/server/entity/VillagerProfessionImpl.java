package net.minestom.server.entity;

import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record VillagerProfessionImpl(RegistryData.VillagerProfessionEntry registry) implements VillagerProfession {
    private static final RegistryData.Container<VillagerProfession> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.VILLAGER_PROFESSIONS,
            (namespace, properties) -> new VillagerProfessionImpl(RegistryData.villagerProfession(namespace, properties)));

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
