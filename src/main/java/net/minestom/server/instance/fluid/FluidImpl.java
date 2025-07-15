package net.minestom.server.instance.fluid;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

record FluidImpl(RegistryData.FluidEntry registry) implements Fluid {
    static final Registry<Fluid> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:fluid"),
            (namespace, properties) -> new FluidImpl(RegistryData.fluid(namespace, properties)));


    static @UnknownNullability Fluid get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public Key key() {
        return registry.key();
    }

    @Override
    public int id() {
        return registry.id();
    }
}
