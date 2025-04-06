package net.minestom.server;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

record FeatureFlagImpl(@NotNull RegistryData.FeatureFlagEntry registry) implements FeatureFlag {

    private static final RegistryData.Container<FeatureFlagImpl> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.FEATURE_FLAGS,
            (namespace, properties) -> new FeatureFlagImpl(RegistryData.featureFlag(namespace, properties)));

    static FeatureFlag get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static FeatureFlag getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static FeatureFlag getId(int id) {
        return CONTAINER.getId(id);
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
