package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Vec;
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

    final class CubicBezier implements EaseFunction {
        private static final int NEWTON_RAPHSON_ITERATIONS = 4;

        private static final Codec<float[]> CONTROL_POINTS_CODEC = Codec.FLOAT.list(4).transform(
                floats -> new float[]{floats.get(0), floats.get(1), floats.get(2), floats.get(3)},
                array -> List.of(array[0], array[1], array[2], array[3]));
        public static final Codec<CubicBezier> CODEC = StructCodec.struct(
                "cubic_bezier", CONTROL_POINTS_CODEC, CubicBezier::controlPoints,
                CubicBezier::new);

        private final float[] controlPoints;
        private final Curve x, y;

        public CubicBezier(float[] controlPoints) {
            Objects.requireNonNull(controlPoints, "controlPoints");
            Check.argCondition(controlPoints.length != 4, "CubicBezier requires 4 control points");
            this.controlPoints = controlPoints;
            this.x = new Curve(controlPoints[0], controlPoints[1]);
            this.y = new Curve(controlPoints[2], controlPoints[3]);
        }

        public float[] controlPoints() {
            return this.controlPoints;
        }

        @Override
        public float sample(float t) {
            float currentT = t;
            for (int i = 0; i < NEWTON_RAPHSON_ITERATIONS; i++) {
                float slope = this.x.sampleGradient(currentT);
                if (slope < Vec.EPSILON) break;

                float error = this.x.sample(currentT) - t;
                currentT -= error / slope;
            }

            return this.y.sample(currentT);
        }

        private record Curve(float a, float b, float c) {
            public Curve(float cp1, float cp2) {
                this(3.0F * cp1 - 3.0F * cp2 + 1.0F, -6.0F * cp1 + 3.0F * cp2, 3.0F * cp1);
            }

            public float sample(float t) {
                return ((a * t + b) * t + c) * t;
            }

            public float sampleGradient(float t) {
                return 3 * a * t * t + 2 * b * t + c;
            }
        }
    }

}
