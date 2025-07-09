package net.minestom.server.instance.fluid;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record FluidImpl(@NotNull RegistryData.FluidEntry registry) implements Fluid {
    static final Registry<Fluid> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.FLUID,
            (namespace, properties) -> new FluidImpl(RegistryData.fluid(namespace, properties)));

    static @UnknownNullability Fluid get(@NotNull RegistryKey<Fluid> key) {
        return REGISTRY.get(key);
    }

    @Override
    public @NotNull Key key() {
        return registry.key();
    }

    @Override
    public int id() {
        return registry.id();
    }
}
