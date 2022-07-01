package net.minestom.server.config;

import org.jetbrains.annotations.Contract;

import java.util.Set;

public sealed interface Config permits ConfigV0 {

    static Builder builder() {
        return new BuilderImpl();
    }

    static Config defaults() {
        return builder().build();
    }

    @Contract("-> new")
    static ConfigParser<Config> parser() {
        return new ConfigParser<>(Set.of(
                VersionInfo.ofLatest(0, ConfigV0.class)
        ), Config.class);
    }

    int compressionThreshold();

    interface Builder {
        Builder compressionThreshold(int compressionThreshold);
        Config build();
    }

    interface Meta {
        int version();
    }
}
