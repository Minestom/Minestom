package net.minestom.server.fluid;

import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Fluid extends StaticProtocolObject, Fluids permits FluidImpl {
    /**
     * Returns the entity registry.
     *
     * @return the entity registry or null if it was created with a builder
     */
    @Contract(pure = true)
    @Nullable
    Registry.FluidEntry registry();

    @Override
    @NotNull
    NamespaceID namespace();

    static @NotNull Collection<@NotNull Fluid> values() {
        return FluidImpl.values();
    }

    static @Nullable Fluid fromNamespaceId(@NotNull String namespaceID) {
        return FluidImpl.getSafe(namespaceID);
    }

    static @Nullable Fluid fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }
}
