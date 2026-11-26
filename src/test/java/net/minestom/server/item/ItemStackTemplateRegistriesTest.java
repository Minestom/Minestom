package net.minestom.server.item;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@RegistriesTest
public class ItemStackTemplateRegistriesTest {

    @Test
    public void bareMaterial(Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        var item = assertOk(ItemStackTemplate.CODEC.decode(coder, new JsonPrimitive("minecraft:diamond")));
        assertEquals(ItemStack.of(Material.DIAMOND), item);

        // A single item encodes as a map without a count.
        assertEquals(JsonParser.parseString("{\"id\":\"minecraft:diamond\"}"), assertOk(ItemStackTemplate.CODEC.encode(coder, item)));
    }

    @Test
    public void countRoundTrip(Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        var json = JsonParser.parseString("{\"id\":\"minecraft:diamond\",\"count\":64}");
        var item = assertOk(ItemStackTemplate.CODEC.decode(coder, json));
        assertEquals(ItemStack.of(Material.DIAMOND, 64), item);
        assertEquals(json, assertOk(ItemStackTemplate.CODEC.encode(coder, item)));

        assertEquals(ItemStack.of(Material.DIAMOND, 99),
                assertOk(ItemStackTemplate.CODEC.decode(coder, JsonParser.parseString("{\"id\":\"minecraft:diamond\",\"count\":99}"))));
    }

    @Test
    public void countOutOfRange(Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        assertInstanceOf(Result.Error.class, ItemStackTemplate.CODEC.decode(coder, JsonParser.parseString("{\"id\":\"minecraft:diamond\",\"count\":0}")));
        assertInstanceOf(Result.Error.class, ItemStackTemplate.CODEC.decode(coder, JsonParser.parseString("{\"id\":\"minecraft:diamond\",\"count\":100}")));
    }
}
