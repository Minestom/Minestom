package net.minestom.server.config;

import com.google.gson.annotations.JsonAdapter;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;

import java.util.Set;

final class Configs {
    static final ConfigParser<Config> PARSER = ConfigParserImpl.create(Set.of(
            VersionInfo.ofLatest(0, Configs.V0.class)
    ), Config.class);

    @JsonAdapter(GsonRecordTypeAdapterFactory.class)
    record V0(int version, int compressionThreshold) implements InternalConfig {
    }

    non-sealed interface InternalConfig extends Config, Config.Meta {
    }
}
