package net.minestom.server.config;

import com.google.gson.annotations.JsonAdapter;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;

final class Configs {
    @JsonAdapter(GsonRecordTypeAdapterFactory.class)
    record V0(int version, int compressionThreshold) implements Config.Meta, Config {
    }
}
