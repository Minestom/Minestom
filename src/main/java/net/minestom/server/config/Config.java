package net.minestom.server.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface Config permits Configs.InternalConfig {

    static @NotNull Builder builder() {
        return new BuilderImpl();
    }

    static @NotNull Config defaults() {
        return builder().build();
    }

    @Contract("-> new")
    static @NotNull ConfigParser<Config> parser() {
        return Configs.PARSER;
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
