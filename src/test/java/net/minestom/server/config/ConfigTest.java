package net.minestom.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    public void gsonSerializationWithMigrationTest() {
        // Setup
        final ConfigManagerImpl<ConfigV2> manager = new ConfigManagerImpl<>(ConfigV2.class, x -> x, x -> x);
        manager.registerVersion(0, ConfigV0.class);
        manager.registerVersion(1, ConfigV1.class);
        manager.registerVersion(2, ConfigV2.class);
        manager.registerMigrationStep(0, x -> new ConfigV1(1, ((ConfigV0) x).a));
        manager.registerMigrationStep(1, x -> new ConfigV2(2, new Foo(((ConfigV1) x).b == 1 ? "one" : "idk")));
        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonRecordTypeAdapterFactory()).create();

        // Load
        final ConfigV2[] configV2 = new ConfigV2[1];
        assertDoesNotThrow(() -> configV2[0] = manager.loadConfig("""
                {"version":0,"a":1,"b":"test"}
                """, gson::fromJson));
        assertEquals("one", configV2[0].a.a);

        // Save
        final String[] serialized = new String[1];
        assertDoesNotThrow(() -> serialized[0] = gson.toJson(manager.clean(configV2[0])));
        assertEquals("""
                {"version":2,"a":{"a":"one"}}""", serialized[0]);
    }

    private record ConfigV0(int version, int a, String b) implements ConfigMeta {}
    private record ConfigV1(int version, int b) implements ConfigMeta {}
    private record Foo(String a) {}
    private record ConfigV2(int version, Foo a) implements ConfigMeta {}
}
