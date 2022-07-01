package net.minestom.server.config;

import com.google.gson.annotations.JsonAdapter;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;

final class Configs {
    @JsonAdapter(GsonRecordTypeAdapterFactory.class)
    record V0(int version, int compressionThreshold) implements InternalConfig {
    }


    non-sealed interface InternalConfig extends Config, Config.Meta {
    }
}
