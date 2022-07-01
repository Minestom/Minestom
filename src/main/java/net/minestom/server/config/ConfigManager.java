package net.minestom.server.config;

public interface ConfigManager<T> {
    <U> T loadConfig(U data, Deserializer<U> loader);

    Object clean(T config);

    @FunctionalInterface
    interface Deserializer<T> {
        <R> R deserialize(T data, Class<R> clazz) throws Throwable;
    }
}
