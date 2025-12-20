package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;

non-sealed interface FloatModifier<Arg> extends EnvironmentAttribute.Modifier<Float, Arg> {
    FloatModifier<AlphaFloat> ALPHA_BLEND = new FloatModifier<>() {
        @java.lang.Override
        public java.lang.Float modify(java.lang.Float sub, AlphaFloat arg) {
            return sub + arg.alpha() * (arg.value() - sub);
        }

        @java.lang.Override
        public Codec<AlphaFloat> argumentCodec() {
            return AlphaFloat.CODEC;
        }
    };
    ToFloat ADD = java.lang.Float::sum;
    ToFloat SUBTRACT = (x, y) -> x - y;
    ToFloat MULTIPLY = (x, y) -> x * y;
    ToFloat MINIMUM = Math::min;
    ToFloat MAXIMUM = Math::max;

    @FunctionalInterface
    interface ToFloat extends FloatModifier<java.lang.Float> {
        @java.lang.Override
        default Codec<java.lang.Float> argumentCodec() {
            return Codec.FLOAT;
        }
    }
}
