package net.minestom.server.entity;

import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

public record VillagerProfessionImpl(RegistryData.VillagerProfessionEntry registry) implements VillagerProfession {
    static final Registry<VillagerProfession> REGISTRY = RegistryData.createStaticRegistry(RegistryKey.unsafeOf("villager_profession"),
            (namespace, properties) -> new VillagerProfessionImpl(RegistryData.villagerProfession(namespace, properties)));

    static @UnknownNullability VillagerProfession get(RegistryKey<VillagerProfession> key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return name();
    }
}
