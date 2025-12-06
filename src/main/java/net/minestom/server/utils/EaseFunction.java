package net.minestom.server.utils;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minestom.server.codec.Codec;

/// The set of ease functions available to the client, with the appropriate names.
///
/// @see Ease Ease for the functions themselves.
public enum EaseFunction {
    CONSTANT(Ease::constant),
    LINEAR(Ease::linear),
    IN_QUAD(Ease::inQuad),
    OUT_QUAD(Ease::outQuad),
    IN_OUT_QUAD(Ease::inOutQuad),
    IN_CUBIC(Ease::inCubic),
    OUT_CUBIC(Ease::outCubic),
    IN_OUT_CUBIC(Ease::inOutCubic),
    IN_QUART(Ease::inQuart),
    OUT_QUART(Ease::outQuart),
    IN_OUT_QUART(Ease::inOutQuart),
    IN_QUINT(Ease::inQuint),
    OUT_QUINT(Ease::outQuint),
    IN_OUT_QUINT(Ease::inOutQuint),
    IN_SINE(Ease::inSine),
    OUT_SINE(Ease::outSine),
    IN_OUT_SINE(Ease::inOutSine),
    IN_EXPO(Ease::inExpo),
    OUT_EXPO(Ease::outExpo),
    IN_OUT_EXPO(Ease::inOutExpo),
    IN_CIRC(Ease::inCirc),
    OUT_CIRC(Ease::outCirc),
    IN_OUT_CIRC(Ease::inOutCirc),
    IN_BACK(Ease::inBack),
    OUT_BACK(Ease::outBack),
    IN_OUT_BACK(Ease::inOutBack),
    IN_ELASTIC(Ease::inElastic);

    public static final Codec<EaseFunction> CODEC = Codec.Enum(EaseFunction.class);

    private final Float2FloatFunction function;

    EaseFunction(Float2FloatFunction function) {
        this.function = function;
    }

    public float apply(float value) {
        return function.apply(value);
    }
}
