package net.minestom.server.featureflag;

import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface FeatureFlag extends StaticProtocolObject permits FeatureFlagImpl {

    /**
     * Returns the entity registry.
     *
     * @return the entity registry or null if it was created with a builder
     */
    @Contract(pure = true)
    @Nullable
    Registry.FeatureFlagEntry registry();

    @Override
    @NotNull
    NamespaceID namespace();

    static @NotNull Collection<@NotNull FeatureFlag> values() {
        return FeatureFlagImpl.values();
    }

    static @Nullable FeatureFlag fromNamespaceId(@NotNull String namespaceID) {
        return FeatureFlagImpl.getSafe(namespaceID);
    }

    static @Nullable FeatureFlag fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

}
