package net.minestom.server;

import net.minestom.server.registry.StaticProtocolObject;

public sealed interface FeatureFlag extends StaticProtocolObject, FeatureFlags permits FeatureFlagImpl {
}
