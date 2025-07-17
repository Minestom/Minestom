package net.minestom.server;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record FeatureFlagImpl(@NotNull RegistryData.FeatureFlagEntry registry) implements FeatureFlag {
    static final Registry<FeatureFlag> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:feature_flag"),
            (namespace, properties) -> new FeatureFlagImpl(RegistryData.featureFlag(namespace, properties)));

    static @UnknownNullability FeatureFlag get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
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
