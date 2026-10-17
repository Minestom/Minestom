package net.minestom.server.world.attribute;

import com.google.gson.JsonObject;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RegistriesTest
public class EnvironmentAttributeMapRegistriesTest {

    @Test
    public void filterSyncableDropsServerOnlyAttributes() {
        var map = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttribute.SKY_LIGHT_FACTOR, 0.5f)
                .set(EnvironmentAttribute.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY)
                .set(EnvironmentAttribute.BED_RULE, BedRule.DESTROY_ON_LEAVE)
                .build();
        var synced = map.filterSyncable();
        assertEquals(EnvironmentAttributeMap.builder().set(EnvironmentAttribute.SKY_LIGHT_FACTOR, 0.5f).build(), synced);
        assertSame(synced, synced.filterSyncable());
    }

    @Test
    public void biomeNetworkCodecOmitsServerOnlyAttributes(Registries registries) {
        Biome plains = registries.biome().get(Biome.PLAINS);
        assertNotNull(plains);
        assertTrue(plains.attributes().entries().containsKey(EnvironmentAttribute.NATURAL_MOB_SPAWNS));

        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        JsonObject attributes = assertOk(Biome.NETWORK_CODEC.encode(coder, plains)).getAsJsonObject().getAsJsonObject("attributes");
        assertNotNull(attributes);
        assertFalse(attributes.has(EnvironmentAttribute.NATURAL_MOB_SPAWNS.key().asString()));
        assertTrue(attributes.has(EnvironmentAttribute.SKY_COLOR.key().asString()));

        JsonObject full = assertOk(Biome.REGISTRY_CODEC.encode(coder, plains)).getAsJsonObject().getAsJsonObject("attributes");
        assertTrue(full.has(EnvironmentAttribute.NATURAL_MOB_SPAWNS.key().asString()));
    }
}
