package net.minestom.server.config;

import java.util.function.Function;

record VersionInfoImpl<T extends Config.Meta>(int version, Class<T> clazz, Function<T, Object> migrator) implements VersionInfo<T> {
}
