package net.minestom.server.config;

public sealed interface Config permits ConfigV0 {

    static Builder builder() {
        return new BuilderImpl();
    }

    static <T> Config load(T data, ConfigHandler.ConfigLoader<T> loader) {
        final ConfigHandler<ConfigV0, Config> handler = new ConfigHandler<>(ConfigV0.class, obj -> obj);
        handler.registerVersion(0, ConfigV0.class);
        return handler.loadConfig(data, loader);
    }

    interface Builder {
        Config build();
    }
}
