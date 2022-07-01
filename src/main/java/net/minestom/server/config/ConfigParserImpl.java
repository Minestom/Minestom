package net.minestom.server.config;

import com.google.gson.annotations.JsonAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

final class ConfigParserImpl<R> implements ConfigParser<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigParserImpl.class);
    private final Function<Config.Meta, R> configFactory;
    private final Class<? extends Config.Meta> latestConfigType;
    private int latestVersion = -1;
    private final Int2ObjectMap<Class<? extends Config.Meta>> configClasses = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Function<Object, Object>> configMigrators = new Int2ObjectOpenHashMap<>();

    ConfigParserImpl(Set<VersionInfo<?>> versionInfoSet, Class<R> configType, Function<Config.Meta, R> configFactory) {
        this.configFactory = configFactory;
        for (VersionInfo<?> info : versionInfoSet) {
            final int v = info.version();
            configClasses.put(v, info.clazz());
            //noinspection unchecked
            configMigrators.put(v, (Function<Object, Object>) info.migrator());
            latestVersion = Math.max(latestVersion, v);
        }
        this.latestConfigType = configClasses.get(latestVersion);
    }

    ConfigParserImpl(Set<VersionInfo<?>> versionInfoSet, Class<R> configType) {
        this(versionInfoSet, configType, configType::cast);
    }

    @Override
    public <T> R loadConfig(T data, Deserializer<T> deserializer, @Nullable Consumer<Object> saveCallback) {
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
                if (function == null)
                    throw new RuntimeException("Migration step missing for %s -> %s".formatted(i, i + 1));
                conf = function.apply(conf);
            }
            if (latestConfigType.isAssignableFrom(conf.getClass())) {
                final Config.Meta cast = latestConfigType.cast(conf);
                if (version != cast.version() && saveCallback != null) {
                    // Version changed, and we have a serializer to write the new version
                    saveCallback.accept(conf);
                }
                return configFactory.apply(cast);
            } else {
                throw new RuntimeException("Latest configuration type mismatch.");
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to load configuration.", throwable);
        }
    }

    @Override
    public <T> R loadConfig(T data, Deserializer<T> deserializer) {
        return loadConfig(data, deserializer, null);
    }

    @JsonAdapter(GsonRecordTypeAdapterFactory.class)
    private record Meta(int version) implements Config.Meta {
    }
}
