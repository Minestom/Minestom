package net.minestom.server.world.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Direction;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@RegistriesTest
public class PositionPredicateRegistriesTest {

    private static PositionPredicate decode(Registries registries, String json) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        return assertOk(PositionPredicate.CODEC.decode(coder, JsonParser.parseString(json)));
    }

    @Test
    public void decodeLeaves(Registries registries) {
        assertEquals(new PositionPredicate.True(), decode(registries, "{\"type\": \"minecraft:true\"}"));
        assertEquals(new PositionPredicate.Solid(Vec.ZERO), decode(registries, "{\"type\": \"minecraft:solid\"}"));
        assertEquals(new PositionPredicate.Solid(new Vec(1, -2, 3)),
                decode(registries, "{\"type\": \"minecraft:solid\", \"offset\": [1, -2, 3]}"));
        assertEquals(new PositionPredicate.MatchingBlockTag(Vec.ZERO, Key.key("dirt")),
                decode(registries, "{\"type\": \"minecraft:matching_block_tag\", \"tag\": \"minecraft:dirt\"}"));
        assertEquals(new PositionPredicate.HasSturdyFace(new Vec(0, -1, 0), Direction.UP),
                decode(registries, "{\"type\": \"minecraft:has_sturdy_face\", \"offset\": [0, -1, 0], \"direction\": \"up\"}"));
        assertEquals(new PositionPredicate.HeightRange(new VerticalAnchor.AboveBottom(4), new VerticalAnchor.Absolute(64)),
                decode(registries, "{\"type\": \"minecraft:height_range\", \"min_inclusive\": {\"above_bottom\": 4}, \"max_inclusive\": {\"absolute\": 64}}"));
    }

    @Test
    public void decodeNested(Registries registries) {
        var solid = new PositionPredicate.Solid(Vec.ZERO);
        assertEquals(new PositionPredicate.Not(solid),
                decode(registries, "{\"type\": \"minecraft:not\", \"predicate\": {\"type\": \"minecraft:solid\"}}"));
        assertEquals(new PositionPredicate.AnyOf(List.of(solid, new PositionPredicate.True())),
                decode(registries, "{\"type\": \"minecraft:any_of\", \"predicates\": [{\"type\": \"minecraft:solid\"}, {\"type\": \"minecraft:true\"}]}"));
        assertEquals(new PositionPredicate.AllOf(List.of()),
                decode(registries, "{\"type\": \"minecraft:all_of\", \"predicates\": []}"));
        assertEquals(new PositionPredicate.VolumeMatch(new Vec(-1, 0, -1), new Vec(1, 0, 1), new PositionPredicate.Not(solid)),
                decode(registries, "{\"type\": \"minecraft:volume_match\", \"min\": [-1, 0, -1], \"max\": [1, 0, 1], "
                        + "\"match\": {\"type\": \"minecraft:not\", \"predicate\": {\"type\": \"minecraft:solid\"}}}"));
    }

    @Test
    public void roundTrip(Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        var predicate = new PositionPredicate.AllOf(List.of(
                new PositionPredicate.Not(new PositionPredicate.Replaceable(new Vec(0, 1, 0))),
                new PositionPredicate.HeightRange(new VerticalAnchor.BelowTop(2), new VerticalAnchor.RelativeToSeaLevel(-8))));
        JsonElement written = assertOk(PositionPredicate.CODEC.encode(coder, predicate));
        assertEquals(predicate, assertOk(PositionPredicate.CODEC.decode(coder, written)));
    }

    @Test
    public void unknownTypeFails(Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        assertInstanceOf(Result.Error.class, PositionPredicate.CODEC.decode(coder, JsonParser.parseString("{\"type\": \"minecraft:missing\"}")));
    }
}
