package net.minestom.server.config;

import com.google.gson.annotations.JsonAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Function;

public final class ConfigParser<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigParser.class);
    private final Function<Object, R> configFactory;
    private final Class<?> latestConfigType;
    private final Function<R, Object> configToRecord;
    private int latestVersion = -1;
    private final Int2ObjectMap<Class<? extends Config.Meta>> configClasses = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Function<Object, Object>> configMigrators = new Int2ObjectOpenHashMap<>();

    public <T> ConfigParser(Set<VersionInfo<?>> versionInfoSet,
                            Class<R> configType, Function<T, R> configFactory, Function<R, Object> configCleaner) {
        this.configToRecord = configCleaner;
        this.configFactory = (Function<Object, R>) configFactory;
        for (VersionInfo<?> info : versionInfoSet) {
            final int v = info.version();
            configClasses.put(v, info.clazz());
            configMigrators.put(v, (Function<Object, Object>) info.migrator());
            latestVersion = Math.max(latestVersion, v);
        }
        this.latestConfigType = configClasses.get(latestVersion);
    }

    public <T> ConfigParser(Set<VersionInfo<?>> versionInfoSet, Class<R> configType) {
        this(versionInfoSet, configType, configType::cast, x -> x);
    }

    public <T> R loadConfig(T data, Deserializer<T> deserializer) {
        try {
            final int version = deserializer.deserialize(data, Meta.class).version();
            final var sourceClass = configClasses.get(version);
            if (sourceClass == null) throw new RuntimeException("Config version isn't supported.");
            final Deprecated deprecated = sourceClass.getAnnotation(Deprecated.class);
            if (deprecated != null) {
                LOGGER.warn("Support for this config version will be removed in the next major version!");
            }
            Object conf = deserializer.deserialize(data, sourceClass);
            for (int i = version; i < latestVersion; i++) {
                final var function = configMigrators.get(i);
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

    public Object clean(R config) {
        return configToRecord.apply(config);
    }

    @JsonAdapter(GsonRecordTypeAdapterFactory.class)
    private record Meta(int version) implements Config.Meta {}

    @FunctionalInterface
    public interface Deserializer<T> {
        <R> R deserialize(T data, Class<R> clazz) throws Throwable;
    }

}
