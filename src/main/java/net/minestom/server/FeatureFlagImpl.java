package net.minestom.server;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record FeatureFlagImpl(RegistryData.FeatureFlagEntry registry) implements FeatureFlag {
    static final Registry<FeatureFlag> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.FEATURE_FLAG,
            (namespace, properties) -> new FeatureFlagImpl(RegistryData.featureFlag(namespace, properties)));

    static @UnknownNullability FeatureFlag get(RegistryKey<FeatureFlag> key) {
        return REGISTRY.get(key);
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
