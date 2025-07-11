package net.minestom.server.entity;

import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public record VillagerProfessionImpl(RegistryData.VillagerProfessionEntry registry) implements VillagerProfession {
    static final Registry<VillagerProfession> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.VILLAGER_PROFESSION,
            (namespace, properties) -> new VillagerProfessionImpl(RegistryData.villagerProfession(namespace, properties)));

    static @UnknownNullability VillagerProfession get(@NotNull RegistryKey<VillagerProfession> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
