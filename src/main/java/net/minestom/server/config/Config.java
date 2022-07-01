package net.minestom.server.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public sealed interface Config permits Configs.InternalConfig {

    static @NotNull Builder builder() {
        return new BuilderImpl();
    }

    static @NotNull Config defaults() {
        return builder().build();
    }

    @Contract("-> new")
    static @NotNull ConfigParser<Config> parser() {
        return new ConfigParserImpl<>(Set.of(
                VersionInfo.ofLatest(0, Configs.V0.class)
        ), Config.class);
    }

    int compressionThreshold();

    interface Builder {
        @NotNull Builder compressionThreshold(int compressionThreshold);

        @NotNull Config build();
    }

    interface Meta {
        int version();
    }
}
