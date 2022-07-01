package net.minestom.server.config;

import com.google.gson.annotations.JsonAdapter;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;

@JsonAdapter(GsonRecordTypeAdapterFactory.class)
record ConfigV0(int version) implements ConfigMeta, Config {
}
