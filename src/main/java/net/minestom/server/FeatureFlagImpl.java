package net.minestom.server;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

record FeatureFlagImpl(@NotNull Registry.FeatureFlagEntry registry) implements FeatureFlag {

    private static final Registry.Container<FeatureFlagImpl> CONTAINER = Registry.createStaticContainer(Registry.Resource.FEATURE_FLAGS,
            (namespace, properties) -> new FeatureFlagImpl(Registry.featureFlag(namespace, properties)));

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
    public @NotNull NamespaceID namespace() {
        return registry.namespace();
    }

    @Override
    public int id() {
        return registry.id();
    }
}
