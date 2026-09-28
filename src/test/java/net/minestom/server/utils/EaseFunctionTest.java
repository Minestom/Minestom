package net.minestom.server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.utils.EaseFunction.CubicBezier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Expected samples were computed independently with the same float precision Newton and bisection solver.
public class EaseFunctionTest {
    private static final float TOLERANCE = 1.0E-6F;

    @Test
    public void sampleCssEase() {
        final CubicBezier ease = new CubicBezier(0.25F, 0.1F, 0.25F, 1.0F);
        assertSample(ease, 0.0F, 0.0F);
        assertSample(ease, 0.1F, 0.094795F);
        assertSample(ease, 0.25F, 0.4085106F);
        assertSample(ease, 0.5F, 0.8024034F);
        assertSample(ease, 0.75F, 0.9604591F);
        assertSample(ease, 0.9F, 0.9943164F);
        assertSample(ease, 1.0F, 1.0F);
    }

    @Test
    public void sampleAsymmetricCurvePairsControlPointsByAxis() {
        // Each x differs from its y, so building an axis from (x1, y1) instead of (x1, x2) gives different samples.
        final CubicBezier bezier = new CubicBezier(0.1F, 0.6F, 0.7F, 0.2F);
        assertSample(bezier, 0.0F, 0.0F);
        assertSample(bezier, 0.1F, 0.24213845F);
        assertSample(bezier, 0.25F, 0.35466403F);
        assertSample(bezier, 0.5F, 0.45442635F);
        assertSample(bezier, 0.75F, 0.60699886F);
        assertSample(bezier, 0.9F, 0.7908324F);
        assertSample(bezier, 1.0F, 1.0000001F);
    }

    @Test
    public void sampleFlatMiddleFallsBackToBisection() {
        // The x gradient is zero at t = 0.5, so Newton steps near x = 0.5 do not converge.
        final CubicBezier bezier = new CubicBezier(1.0F, 0.0F, 0.0F, 1.0F);
        assertSample(bezier, 0.0F, 0.0F);
        assertSample(bezier, 0.25F, 0.029723404F);
        assertSample(bezier, 0.45F, 0.17687753F);
        assertSample(bezier, 0.49F, 0.30145666F);
        assertSample(bezier, 0.5F, 0.5F);
        assertSample(bezier, 0.51F, 0.6985431F);
        assertSample(bezier, 0.75F, 0.97027665F);
        assertSample(bezier, 1.0F, 1.0F);
    }

    @Test
    public void sampleFlatEndsFallsBackToBisection() {
        // The x gradient is zero at t = 0 and t = 1, so Newton steps near x = 1 do not converge.
        final CubicBezier bezier = new CubicBezier(0.0F, 1.0F, 1.0F, 0.0F);
        assertSample(bezier, 0.0F, 0.0F);
        assertSample(bezier, 0.1F, 0.38741058F);
        assertSample(bezier, 0.5F, 0.5F);
        assertSample(bezier, 0.999F, 0.9466389F);
        assertSample(bezier, 1.0F, 1.0F);
    }

    @Test
    public void codecRoundTripPreservesFloats() {
        final CubicBezier bezier = new CubicBezier(0.1F, -0.3333333F, 0.9F, 1.7F);

        final BinaryTag nbt = CubicBezier.CODEC.encode(Transcoder.NBT, bezier).orElseThrow();
        assertExactControlPoints(bezier, CubicBezier.CODEC.decode(Transcoder.NBT, nbt).orElseThrow());

        final JsonElement json = EaseFunction.CODEC.encode(Transcoder.JSON, bezier).orElseThrow();
        assertExactControlPoints(bezier, assertInstanceOf(CubicBezier.class,
                EaseFunction.CODEC.decode(Transcoder.JSON, json).orElseThrow()));
    }

    @Test
    public void codecUsesControlPointOrder() {
        final JsonElement json = JsonParser.parseString("{\"cubic_bezier\": [0.25, 0.1, 0.5, 1.0]}");
        final CubicBezier decoded = CubicBezier.CODEC.decode(Transcoder.JSON, json).orElseThrow();
        assertExactControlPoints(new CubicBezier(0.25F, 0.1F, 0.5F, 1.0F), decoded);

        final JsonElement encoded = CubicBezier.CODEC.encode(Transcoder.JSON, decoded).orElseThrow();
        final JsonArray controlPoints = encoded.getAsJsonObject().getAsJsonArray("cubic_bezier");
        assertEquals(4, controlPoints.size());
        assertEquals(0.25F, controlPoints.get(0).getAsFloat());
        assertEquals(0.1F, controlPoints.get(1).getAsFloat());
        assertEquals(0.5F, controlPoints.get(2).getAsFloat());
        assertEquals(1.0F, controlPoints.get(3).getAsFloat());
    }

    @Test
    public void codecRejectsXOutsideUnitRange() {
        assertDecodeError("[-0.1, 0.0, 0.5, 1.0]", "x1 must be between 0 and 1");
        assertDecodeError("[1.1, 0.0, 0.5, 1.0]", "x1 must be between 0 and 1");
        assertDecodeError("[0.5, 0.0, -0.1, 1.0]", "x2 must be between 0 and 1");
        assertDecodeError("[0.5, 0.0, 1.1, 1.0]", "x2 must be between 0 and 1");

        final JsonElement json = JsonParser.parseString("{\"cubic_bezier\": [1.1, 0.0, 0.5, 1.0]}");
        assertInstanceOf(Result.Error.class, EaseFunction.CODEC.decode(Transcoder.JSON, json));
    }

    @Test
    public void codecAcceptsUnitBoundsAndUnboundedY() {
        final JsonElement json = JsonParser.parseString("{\"cubic_bezier\": [0.0, -2.0, 1.0, 3.0]}");
        final CubicBezier decoded = CubicBezier.CODEC.decode(Transcoder.JSON, json).orElseThrow();
        assertExactControlPoints(new CubicBezier(0.0F, -2.0F, 1.0F, 3.0F), decoded);
    }

    @Test
    public void constructorRejectsXOutsideUnitRange() {
        assertThrows(IllegalArgumentException.class, () -> new CubicBezier(-0.1F, 0.0F, 0.5F, 1.0F));
        assertThrows(IllegalArgumentException.class, () -> new CubicBezier(1.1F, 0.0F, 0.5F, 1.0F));
        assertThrows(IllegalArgumentException.class, () -> new CubicBezier(0.5F, 0.0F, -0.1F, 1.0F));
        assertThrows(IllegalArgumentException.class, () -> new CubicBezier(0.5F, 0.0F, 1.1F, 1.0F));
    }

    @Test
    public void equality() {
        final CubicBezier bezier = new CubicBezier(0.25F, 0.1F, 0.25F, 1.0F);
        final CubicBezier same = new CubicBezier(0.25F, 0.1F, 0.25F, 1.0F);
        assertEquals(bezier, same);
        assertEquals(bezier.hashCode(), same.hashCode());

        assertNotEquals(bezier, new CubicBezier(0.1F, 0.25F, 0.25F, 1.0F));
        assertNotEquals(bezier, new CubicBezier(0.25F, 0.1F, 1.0F, 0.25F));
    }

    // Covers the deprecated array bridges until they are removed.
    @Test
    @Deprecated
    public void deprecatedArrayBridges() {
        final float[] controlPoints = {0.1F, 0.6F, 0.7F, 0.2F};
        final CubicBezier bezier = new CubicBezier(controlPoints);
        assertEquals(new CubicBezier(0.1F, 0.6F, 0.7F, 0.2F), bezier);
        assertArrayEquals(controlPoints, bezier.controlPoints());

        controlPoints[0] = 0.9F;
        bezier.controlPoints()[1] = 0.9F;
        assertArrayEquals(new float[]{0.1F, 0.6F, 0.7F, 0.2F}, bezier.controlPoints());

        assertThrows(IllegalArgumentException.class, () -> new CubicBezier(new float[]{0.1F, 0.6F, 0.7F}));
        assertThrows(IllegalArgumentException.class, () -> new CubicBezier(new float[]{1.1F, 0.6F, 0.7F, 0.2F}));
    }

    private static void assertSample(EaseFunction function, float x, float expected) {
        assertEquals(expected, function.sample(x), TOLERANCE, () -> "sample(" + x + ")");
    }

    private static void assertExactControlPoints(CubicBezier expected, CubicBezier actual) {
        assertEquals(expected.x1(), actual.x1());
        assertEquals(expected.y1(), actual.y1());
        assertEquals(expected.x2(), actual.x2());
        assertEquals(expected.y2(), actual.y2());
    }

    private static void assertDecodeError(String controlPoints, String expectedMessage) {
        final JsonElement json = JsonParser.parseString("{\"cubic_bezier\": " + controlPoints + "}");
        final Result<CubicBezier> result = CubicBezier.CODEC.decode(Transcoder.JSON, json);
        final String message = assertInstanceOf(Result.Error.class, result).message();
        assertTrue(message.contains(expectedMessage), message);
    }
}
