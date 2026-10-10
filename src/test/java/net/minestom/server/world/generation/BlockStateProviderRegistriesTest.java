package net.minestom.server.world.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Range;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

@RegistriesTest
public class BlockStateProviderRegistriesTest {

    @Test
    public void vanillaEntries(Registries registries) {
        var registry = registries.blockStateProvider();
        assertEquals(8, registry.size());

        var expected = new BlockStateProvider.RuleBased(null, List.of(new BlockStateProvider.RuleBased.Rule(
                new PositionPredicate.MatchingBlockTag(Vec.ZERO, Key.key("beneath_tree_podzol_replaceable")),
                new BlockStateProvider.Simple(Block.PODZOL))));
        assertEquals(expected, registry.get(BlockStateProvider.PODZOL_BENEATH_TREE));

        var meadow = assertInstanceOf(BlockStateProvider.DualNoise.class, registry.get(BlockStateProvider.FLOWER_MEADOW));
        assertEquals(new Range.Int(1, 3), meadow.variety());
    }

    @Test
    public void holderForms(Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);

        var referenceJson = new JsonPrimitive("minecraft:cave_vines_body");
        Holder<BlockStateProvider> reference = assertOk(BlockStateProvider.CODEC.decode(coder, referenceJson));
        assertEquals(BlockStateProvider.CAVE_VINES_BODY, reference);
        assertNull(reference.asValue());
        assertEquals(referenceJson, assertOk(BlockStateProvider.CODEC.encode(coder, reference)));

        var stateJson = JsonParser.parseString("{\"id\":\"minecraft:farmland\",\"properties\":{\"moisture\":\"3\"}}");
        Holder<BlockStateProvider> state = assertOk(BlockStateProvider.CODEC.decode(coder, stateJson));
        assertEquals(new BlockStateProvider.Simple(Block.FARMLAND.withProperty("moisture", "3")), state);
        JsonElement written = assertOk(BlockStateProvider.CODEC.encode(coder, state));
        assertEquals(JsonParser.parseString("{\"id\":\"farmland\",\"properties\":{\"moisture\":\"3\"}}"), written);
        assertEquals(state, assertOk(BlockStateProvider.CODEC.decode(coder, written)));

        var typedJson = JsonParser.parseString("{\"type\":\"minecraft:copy_properties\",\"source\":\"minecraft:cave_vines_head\"}");
        Holder<BlockStateProvider> typed = assertOk(BlockStateProvider.CODEC.decode(coder, typedJson));
        assertEquals(new BlockStateProvider.CopyProperties(BlockStateProvider.CAVE_VINES_HEAD), typed);
        assertEquals(typed, assertOk(BlockStateProvider.CODEC.decode(coder, assertOk(BlockStateProvider.CODEC.encode(coder, typed)))));
    }

    @Test
    public void varietyForms() {
        var codec = BlockStateProvider.DualNoise.VARIETY_CODEC;
        assertEquals(new Range.Int(2, 2), assertOk(codec.decode(Transcoder.JSON, new JsonPrimitive(2))));
        assertEquals(new Range.Int(1, 3), assertOk(codec.decode(Transcoder.JSON, JsonParser.parseString("[1, 3]"))));
        assertEquals(new Range.Int(1, 3), assertOk(codec.decode(Transcoder.JSON,
                JsonParser.parseString("{\"min_inclusive\": 1, \"max_inclusive\": 3}"))));
        assertInstanceOf(Result.Error.class, codec.decode(Transcoder.JSON, JsonParser.parseString("[1, 2, 3]")));

        assertEquals(new JsonPrimitive(2), assertOk(codec.encode(Transcoder.JSON, new Range.Int(2, 2))));
        assertEquals(JsonParser.parseString("[1, 3]"), assertOk(codec.encode(Transcoder.JSON, new Range.Int(1, 3))));
    }
}
