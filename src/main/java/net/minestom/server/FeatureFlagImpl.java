package net.minestom.server;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

record FeatureFlagImpl(@NotNull Registry.FeatureFlagEntry registry) implements FeatureFlag {

    private static final Registry.Container<FeatureFlagImpl> CONTAINER = Registry.createStaticContainer(Registry.Resource.FEATURE_FLAGS,
            (key, properties) -> new FeatureFlagImpl(Registry.featureFlag(key, properties)));

    static FeatureFlag get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static FeatureFlag getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
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
