package net.minestom.server.world.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.world.DimensionType;
import org.junit.jupiter.api.Test;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VerticalAnchorTest {

    private static void assertRoundTrip(String json, VerticalAnchor expected) {
        JsonElement element = JsonParser.parseString(json);
        assertEquals(expected, assertOk(VerticalAnchor.CODEC.decode(Transcoder.JSON, element)));
        assertEquals(element, assertOk(VerticalAnchor.CODEC.encode(Transcoder.JSON, expected)));
    }

    @Test
    public void roundTrip() {
        assertRoundTrip("{\"absolute\": 64}", new VerticalAnchor.Absolute(64));
        assertRoundTrip("{\"above_bottom\": 8}", new VerticalAnchor.AboveBottom(8));
        assertRoundTrip("{\"below_top\": -3}", new VerticalAnchor.BelowTop(-3));
        assertRoundTrip("{\"relative_to_sea_level\": 0}", new VerticalAnchor.RelativeToSeaLevel(0));
    }

    @Test
    public void outOfRangeDecodeFails() {
        for (String field : new String[]{"absolute", "above_bottom", "below_top", "relative_to_sea_level"}) {
            JsonElement tooLow = JsonParser.parseString("{\"" + field + "\": " + (DimensionType.MIN_Y - 1) + "}");
            assertInstanceOf(Result.Error.class, VerticalAnchor.CODEC.decode(Transcoder.JSON, tooLow), field);
            JsonElement tooHigh = JsonParser.parseString("{\"" + field + "\": " + (DimensionType.MAX_Y + 1) + "}");
            assertInstanceOf(Result.Error.class, VerticalAnchor.CODEC.decode(Transcoder.JSON, tooHigh), field);
        }
    }

    @Test
    public void constructorBounds() {
        assertDoesNotThrow(() -> new VerticalAnchor.Absolute(DimensionType.MIN_Y));
        assertDoesNotThrow(() -> new VerticalAnchor.Absolute(DimensionType.MAX_Y));
        assertThrows(IllegalArgumentException.class, () -> new VerticalAnchor.Absolute(DimensionType.MIN_Y - 1));
        assertThrows(IllegalArgumentException.class, () -> new VerticalAnchor.AboveBottom(DimensionType.MAX_Y + 1));
        assertThrows(IllegalArgumentException.class, () -> new VerticalAnchor.BelowTop(DimensionType.MIN_Y - 1));
        assertThrows(IllegalArgumentException.class, () -> new VerticalAnchor.RelativeToSeaLevel(DimensionType.MAX_Y + 1));
    }
}
