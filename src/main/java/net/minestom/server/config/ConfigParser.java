package net.minestom.server.config;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public sealed interface ConfigParser<R> permits ConfigParserImpl {

    /**
     * Used to load a serialized config into {@link R}
     *
     * @param data serialized data (source)
     * @param deserializer serializer which has to tolerate partial reads i.e. when the passed class doesn't consume
     *                     all available data; for JSON data you can use e.g.
     *                     {@link com.google.gson.Gson#fromJson(String, Type)} here
     * @param saveCallback executed when the serialized version is older and a new version should be written
     * @return the latest config
     * @param <T> type of serialized data (source)
     */
    <T> R loadConfig(T data, ConfigParserImpl.Deserializer<T> deserializer, @Nullable Consumer<Object> saveCallback);

    /**
     * @see #loadConfig(Object, ConfigParserImpl.Deserializer, Consumer)
     */
    <T> R loadConfig(T data, ConfigParserImpl.Deserializer<T> deserializer);

    @FunctionalInterface
    interface Deserializer<T> {
        <R> R deserialize(T data, Class<R> clazz) throws Throwable;
    }
}
