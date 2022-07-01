package net.minestom.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.utils.GsonRecordTypeAdapterFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    public void gsonSerializationWithMigrationTest() {
        // Setup
        final ConfigParser<Conf> parser = new ConfigParser<>(Set.of(
                VersionInfo.of(0, ConfigV0.class, x -> new ConfigV1(1, x.a)),
                VersionInfo.of(1, ConfigV1.class, x -> new ConfigV2(2, new Foo(x.b == 1 ? "one" : "idk"))),
                VersionInfo.ofLatest(2, ConfigV2.class)
        ), Conf.class);
        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonRecordTypeAdapterFactory()).create();

        // Load
        final Conf[] configV2 = new Conf[1];
        assertDoesNotThrow(() -> configV2[0] = parser.loadConfig("{\"version\":0,\"a\":1,\"b\":\"test\"}",
                gson::fromJson, n -> {
                    // Save
                    final String[] serialized = new String[1];
                    assertDoesNotThrow(() -> serialized[0] = gson.toJson(n));
                    assertEquals("{\"version\":2,\"a\":{\"a\":\"one\"}}", serialized[0]);
                }));
        assertEquals("one", configV2[0].a().a);
    }

    private record ConfigV0(int version, int a, String b) implements Config.Meta {
    }

    private record ConfigV1(int version, int b) implements Config.Meta {
    }

    private record Foo(String a) {
    }

    private record ConfigV2(int version, Foo a) implements Config.Meta, Conf {
    }

    private interface Conf {
        Foo a();
    }
}
