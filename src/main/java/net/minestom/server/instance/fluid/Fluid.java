package net.minestom.server.instance.fluid;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public sealed interface Fluid extends StaticProtocolObject<Fluid>, Fluids permits FluidImpl {

    @Override
    RegistryData.FluidEntry registry();

    static Collection<Fluid> values() {
        return FluidImpl.REGISTRY.values();
    }

    static @Nullable Fluid fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable Fluid fromKey(Key key) {
        return FluidImpl.REGISTRY.get(key);
    }

    static @Nullable Fluid fromId(int id) {
        return FluidImpl.REGISTRY.get(id);
    }

    static Registry<Fluid> staticRegistry() {
        return FluidImpl.REGISTRY;
    }
}
