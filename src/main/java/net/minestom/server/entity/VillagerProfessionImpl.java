package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public record VillagerProfessionImpl(RegistryData.VillagerProfessionEntry registry) implements VillagerProfession {
    static final StaticRegistry<VillagerProfession> REGISTRY = RegistryData.createStaticRegistry(
            RegistryData.Resource.VILLAGER_PROFESSIONS, "minecraft:villager_profession",
            (namespace, properties) -> new VillagerProfessionImpl(RegistryData.villagerProfession(namespace, properties)));

    static @UnknownNullability VillagerProfession get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }
}
