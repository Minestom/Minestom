package net.minestom.server.config;

import java.util.function.Function;

public interface VersionInfo<T extends Config.Meta> {

    static <U extends Config.Meta> VersionInfo<U> of(int version, Class<U> clazz, Function<U, Object> migrator) {
        return new VersionInfoImpl<>(version, clazz, migrator);
    }

    static <U extends Config.Meta> VersionInfo<U> of(int version, Class<U> clazz) {
        return of(version, clazz, null);
    }

    int version();
    Class<T> clazz();
    Function<T, Object> migrator();
}
