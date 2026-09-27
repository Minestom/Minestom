package net.minestom.server.utils;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.utils.validate.Check;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/// The set of ease functions available to the client, with the appropriate names.
///
/// @see Ease Ease for the functions themselves.
public interface EaseFunction {
    EaseFunction CONSTANT = Ease::constant;
    EaseFunction LINEAR = Ease::linear;
    EaseFunction IN_QUAD = Ease::inQuad;
    EaseFunction OUT_QUAD = Ease::outQuad;
    EaseFunction IN_OUT_QUAD = Ease::inOutQuad;
    EaseFunction IN_CUBIC = Ease::inCubic;
    EaseFunction OUT_CUBIC = Ease::outCubic;
    EaseFunction IN_OUT_CUBIC = Ease::inOutCubic;
    EaseFunction IN_QUART = Ease::inQuart;
    EaseFunction OUT_QUART = Ease::outQuart;
    EaseFunction IN_OUT_QUART = Ease::inOutQuart;
    EaseFunction IN_QUINT = Ease::inQuint;
    EaseFunction OUT_QUINT = Ease::outQuint;
    EaseFunction IN_OUT_QUINT = Ease::inOutQuint;
    EaseFunction IN_SINE = Ease::inSine;
    EaseFunction OUT_SINE = Ease::outSine;
    EaseFunction IN_OUT_SINE = Ease::inOutSine;
    EaseFunction IN_EXPO = Ease::inExpo;
    EaseFunction OUT_EXPO = Ease::outExpo;
    EaseFunction IN_OUT_EXPO = Ease::inOutExpo;
    EaseFunction IN_CIRC = Ease::inCirc;
    EaseFunction OUT_CIRC = Ease::outCirc;
    EaseFunction IN_OUT_CIRC = Ease::inOutCirc;
    EaseFunction IN_BACK = Ease::inBack;
    EaseFunction OUT_BACK = Ease::outBack;
    EaseFunction IN_OUT_BACK = Ease::inOutBack;
    EaseFunction IN_ELASTIC = Ease::inElastic;

    // Only contains the named functions
    Map<String, EaseFunction> NAMED_BY_KEY = Map.ofEntries(
            Map.entry("constant", CONSTANT),
            Map.entry("linear", LINEAR),
            Map.entry("in_quad", IN_QUAD),
            Map.entry("out_quad", OUT_QUAD),
            Map.entry("in_out_quad", IN_OUT_QUAD),
            Map.entry("in_cubic", IN_CUBIC),
            Map.entry("out_cubic", OUT_CUBIC),
            Map.entry("in_out_cubic", IN_OUT_CUBIC),
            Map.entry("in_quart", IN_QUART),
            Map.entry("out_quart", OUT_QUART),
            Map.entry("in_out_quart", IN_OUT_QUART),
            Map.entry("in_quint", IN_QUINT),
            Map.entry("out_quint", OUT_QUINT),
            Map.entry("in_out_quint", IN_OUT_QUINT),
            Map.entry("in_sine", IN_SINE),
            Map.entry("out_sine", OUT_SINE),
            Map.entry("in_out_sine", IN_OUT_SINE),
            Map.entry("in_expo", IN_EXPO),
            Map.entry("out_expo", OUT_EXPO),
            Map.entry("in_out_expo", IN_OUT_EXPO),
            Map.entry("in_circ", IN_CIRC),
            Map.entry("out_circ", OUT_CIRC),
            Map.entry("in_out_circ", IN_OUT_CIRC),
            Map.entry("in_back", IN_BACK),
            Map.entry("out_back", OUT_BACK),
            Map.entry("in_out_back", IN_OUT_BACK),
            Map.entry("in_elastic", IN_ELASTIC)
    );
    Map<EaseFunction, String> NAMED_BY_VALUE = NAMED_BY_KEY.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    Codec<EaseFunction> CODEC = Codec.Either(Codec.STRING.transform(NAMED_BY_KEY::get, NAMED_BY_VALUE::get), CubicBezier.CODEC)
            .transform(either -> either.unify(f -> f, f -> f),
                    f -> f instanceof CubicBezier bezier ? Either.right(bezier) : Either.left(f));

    float sample(float value);

    /// A cubic bezier ease from (0, 0) to (1, 1), shaped by the control points (x1, y1) and (x2, y2).
    ///
    /// Sampling treats the input as an x coordinate, finds the point of the curve with that x, and returns its
    /// y coordinate. Both x coordinates must be between 0 and 1 inclusive, which keeps x from decreasing along the
    /// curve so that each input has one output. The y coordinates are unbounded, and values outside 0 to 1 make
    /// the ease overshoot.
    ///
    /// [#CODEC] reads and writes the control points as `{"cubic_bezier": [x1, y1, x2, y2]}`. Decoding returns a
    /// [Result.Error] when x1 or x2 is outside 0 to 1.
    ///
    /// @param x1 the x coordinate of the first control point, between 0 and 1 inclusive
    /// @param y1 the y coordinate of the first control point
    /// @param x2 the x coordinate of the second control point, between 0 and 1 inclusive
    /// @param y2 the y coordinate of the second control point
    record CubicBezier(float x1, float y1, float x2, float y2) implements EaseFunction {
        private static final int NEWTON_RAPHSON_ITERATIONS = 4;
        private static final float MAX_NEWTON_STEP = 0.25F;
        private static final float EPSILON = 1.0E-5F;

        private static final Codec<CubicBezier> CONTROL_POINTS_CODEC = Codec.FLOAT.list(4).transform(
                floats -> new CubicBezier(floats.get(0), floats.get(1), floats.get(2), floats.get(3)),
                bezier -> List.of(bezier.x1(), bezier.y1(), bezier.x2(), bezier.y2()));
        public static final Codec<CubicBezier> CODEC = StructCodec.struct(
                "cubic_bezier", CONTROL_POINTS_CODEC, bezier -> bezier,
                bezier -> bezier);

        /// Creates a cubic bezier ease from its control points.
        ///
        /// @throws IllegalArgumentException if x1 or x2 is outside 0 to 1
        public CubicBezier {
            if (x1 < 0.0F || x1 > 1.0F) throw new IllegalArgumentException("x1 must be between 0 and 1, got " + x1);
            if (x2 < 0.0F || x2 > 1.0F) throw new IllegalArgumentException("x2 must be between 0 and 1, got " + x2);
        }

        /// Creates a cubic bezier ease from an array holding x1, y1, x2, and y2 in that order.
        ///
        /// @param controlPoints the control points, must have a length of 4
        /// @throws IllegalArgumentException if the array length is not 4, or if x1 or x2 is outside 0 to 1
        /// @deprecated use [#CubicBezier(float, float, float, float)] instead
        @Deprecated
        public CubicBezier(float[] controlPoints) {
            Objects.requireNonNull(controlPoints, "controlPoints");
            Check.argCondition(controlPoints.length != 4, "CubicBezier requires 4 control points");
            this(controlPoints[0], controlPoints[1], controlPoints[2], controlPoints[3]);
        }

        /// Returns the control points as a new array holding x1, y1, x2, and y2 in that order.
        ///
        /// @return a new array of the control points
        /// @deprecated use [#x1()], [#y1()], [#x2()], and [#y2()] instead
        @Deprecated
        public float[] controlPoints() {
            return new float[]{x1, y1, x2, y2};
        }

        @Override
        public float sample(float x) {
            final Curve xCurve = new Curve(x1, x2);
            final Curve yCurve = new Curve(y1, y2);
            return yCurve.sample(xCurve.solve(x));
        }

        private record Curve(float a, float b, float c) {
            Curve(float cp1, float cp2) {
                this(3.0F * cp1 - 3.0F * cp2 + 1.0F, -6.0F * cp1 + 3.0F * cp2, 3.0F * cp1);
            }

            float sample(float t) {
                return ((a * t + b) * t + c) * t;
            }

            float sampleGradient(float t) {
                return (3.0F * a * t + 2.0F * b) * t + c;
            }

            // Finds the t where the curve reaches value. Newton steps run first, and bisection takes
            // over when they do not converge or the gradient is too flat to follow.
            float solve(float value) {
                float t = value;
                for (int i = 0; i < NEWTON_RAPHSON_ITERATIONS; i++) {
                    final float error = sample(t) - value;
                    if (Math.abs(error) < EPSILON) return t;
                    final float gradient = sampleGradient(t);
                    if (gradient < EPSILON) break;
                    t -= Math.clamp(error / gradient, -MAX_NEWTON_STEP, MAX_NEWTON_STEP);
                }
                return bisect(value, t);
            }

            float bisect(float value, float t) {
                float low = 0.0F, high = 1.0F;
                while (low < high) {
                    final float error = sample(t) - value;
                    if (Math.abs(error) < EPSILON) return t;
                    if (error < 0.0F) low = t;
                    else high = t;
                    t = (low + high) / 2.0F;
                }
                return t;
            }
        }
    }

}
