package net.minestom.server.featureflag;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public record FeatureFlagImpl(Registry.FeatureFlagEntry registry, NamespaceID namespace, int id) implements FeatureFlag {
    private static final AtomicInteger INDEX = new AtomicInteger();
    private static final Registry.DynamicContainer<FeatureFlag> CONTAINER = Registry.createDynamicContainer(Registry.Resource.FEATURE_FLAGS, FeatureFlagImpl::createImpl);

    private static FeatureFlagImpl createImpl(String namespace, Registry.Properties properties) {
        return new FeatureFlagImpl(Registry.featureFlag(namespace, properties));
    }

    private FeatureFlagImpl(Registry.FeatureFlagEntry registry) {
        this(registry, registry.namespace(), INDEX.getAndIncrement());
    }

    static Collection<FeatureFlag> values() {
        return CONTAINER.values();
    }

    public static FeatureFlag get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static FeatureFlag getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }
}
