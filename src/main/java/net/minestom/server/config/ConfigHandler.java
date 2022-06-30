package net.minestom.server.config;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@ApiStatus.Internal
public final class ConfigHandler<L extends ConfigMeta, R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHandler.class);
    private final Function<L, R> configFactory;
    private final Class<L> latestConfigType;
    private int latestVersion = -1;
    private final Map<Integer, Class<? extends ConfigMeta>> configClasses = new HashMap<>();
    private final Map<Integer, Function<Object, Object>> configMigrators = new HashMap<>();

    public ConfigHandler(Class<L> latestConfigType, Function<L, R> configFactory) {
        this.configFactory = configFactory;
        this.latestConfigType = latestConfigType;
    }

    public void registerVersion(int version, Class<? extends ConfigMeta> clazz) {
        configClasses.put(version, clazz);
        latestVersion = Math.max(latestVersion, version);
    }

    public void registerMigrationStep(int fromVersion, Function<Object, Object> migrator) {
        configMigrators.put(fromVersion, migrator);
    }

    public <T> R loadConfig(T data, ConfigLoader<T> loader) {
        try {
            final int version = loader.load(data, Meta.class).version();
            final Class<? extends ConfigMeta> sourceClass = configClasses.get(version);
            if (sourceClass == null) throw new RuntimeException("Config version isn't supported.");
            final Deprecated deprecated = sourceClass.getAnnotation(Deprecated.class);
            if (deprecated != null) {
                LOGGER.warn("Support for this config version will be removed in the next major version!");
            }
            Object conf = loader.load(data, sourceClass);
            for (int i = version; i < latestVersion; i++) {
                final Function<Object, Object> function = configMigrators.get(i);
                if (function == null) throw new RuntimeException("Migration step missing for %s -> %s".formatted(i, i+1));
                conf = function.apply(conf);
            }
            if (latestConfigType.isAssignableFrom(conf.getClass())) {
                return configFactory.apply(latestConfigType.cast(conf));
            } else {
                throw new RuntimeException("Latest configuration type mismatch.");
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to load configuration.", throwable);
        }
    }

    public interface ConfigLoader<T> {
        <R extends ConfigMeta> R load(T data, Class<R> clazz) throws Throwable;
    }
    private record Meta(int version) implements ConfigMeta {}
}
