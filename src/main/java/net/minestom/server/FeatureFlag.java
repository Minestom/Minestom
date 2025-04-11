package net.minestom.server;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface FeatureFlag extends StaticProtocolObject, FeatureFlags permits FeatureFlagImpl {

    static @NotNull Collection<@NotNull FeatureFlag> values() {
        return FeatureFlagImpl.REGISTRY.values();
    }

    static @Nullable FeatureFlag fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable FeatureFlag fromKey(@NotNull Key key) {
        return FeatureFlagImpl.REGISTRY.get(key);
    }

    static @Nullable FeatureFlag fromId(int id) {
        return FeatureFlagImpl.REGISTRY.get(id);
    }

}
