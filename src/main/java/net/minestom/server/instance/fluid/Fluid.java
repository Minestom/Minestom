package net.minestom.server.instance.fluid;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Fluid extends StaticProtocolObject<Fluid>, Fluids permits FluidImpl {

    /**
     * Returns the legacy registry data backing this fluid.
     *
     * @return the legacy registry data
     * @deprecated registry data will no longer be exposed
    */
    @Override
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
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
