package net.minestom.server.config;

import org.jetbrains.annotations.Contract;

public sealed interface Config permits ConfigV0 {

    static Builder builder() {
        return new BuilderImpl();
    }

    static Config defaults() {
        return builder().build();
    }

    @Contract("-> new")
    static ConfigManager<Config> manager() {
        final ConfigManagerImpl<Config> manager = new ConfigManagerImpl<>(ConfigV0.class, x -> x, x -> x);
        manager.registerVersion(0, ConfigV0.class);
        return manager;
    }

    int compressionThreshold();

    interface Builder {
        Builder compressionThreshold(int compressionThreshold);
        Config build();
    }
}
