package net.minestom.server.config;

import java.util.function.Function;

/**
 * Serialization information for a given version
 * @param <T> type of the version
 */
public interface VersionInfo<T extends Config.Meta> {

    static <U extends Config.Meta> VersionInfo<U> of(int version, Class<U> clazz, Function<U, Object> migrator) {
        return new VersionInfoImpl<>(version, clazz, migrator);
    }

    static <U extends Config.Meta> VersionInfo<U> ofLatest(int version, Class<U> clazz) {
        return of(version, clazz, null);
    }

    int version();
    Class<T> clazz();

    /**
     * Used to migrate old configs by {@link ConfigParser#loadConfig(Object, ConfigParser.Deserializer)}, the
     * returned object must be assignable to the type of proceeding version's {@link T}
     */
    Function<T, Object> migrator();
}
