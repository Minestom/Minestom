package net.minestom.server.world.attribute;

import net.kyori.adventure.util.ARGBLike;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.utils.Either;

non-sealed interface ColorModifier<Arg> extends EnvironmentAttribute.Modifier<RGBLike, Arg> {
    Codec<RGBLike> MAYBE_ARGB_CODEC = Codec.Either(AlphaColor.STRING_CODEC, net.minestom.server.color.Color.STRING_CODEC).transform(
            either -> either.unify(c -> c, c -> c),
            color -> color instanceof ARGBLike argb && argb.alpha() != 255 ? Either.left(argb) : Either.right(color));

    ColorModifier<ARGBLike> ALPHA_BLEND = new ColorModifier<>() {
        @java.lang.Override
        public RGBLike modify(RGBLike subject, ARGBLike argument) {
            throw new UnsupportedOperationException("alpha blend is not implemented yet");
        }

        @java.lang.Override
        public Codec<ARGBLike> argumentCodec() {
            return AlphaColor.STRING_CODEC;
        }
    };
    ColorModifier<RGBLike> ADD = new ColorModifier<>() {
        @java.lang.Override
        public RGBLike modify(RGBLike subject, RGBLike argument) {
            return new AlphaColor(
                    subject instanceof ARGBLike argb ? argb.alpha() : 255,
                    Math.min(255, subject.red() + argument.red()),
                    Math.min(255, subject.green() + argument.green()),
                    Math.min(255, subject.blue() + argument.blue())
            );
        }

        @java.lang.Override
        public Codec<RGBLike> argumentCodec() {
            return MAYBE_ARGB_CODEC;
        }
    };
    ColorModifier<RGBLike> SUBTRACT = new ColorModifier<>() {
        @java.lang.Override
        public RGBLike modify(RGBLike subject, RGBLike argument) {
            return new AlphaColor(
                    subject instanceof ARGBLike argb ? argb.alpha() : 255,
                    Math.max(0, subject.red() - argument.red()),
                    Math.max(0, subject.green() - argument.green()),
                    Math.max(0, subject.blue() - argument.blue())
            );
        }

        @java.lang.Override
        public Codec<RGBLike> argumentCodec() {
            return MAYBE_ARGB_CODEC;
        }
    };
    ColorModifier<RGBLike> MULTIPLY_RGB = new ColorModifier<>() {
        @java.lang.Override
        public RGBLike modify(RGBLike subject, RGBLike argument) {
            int subAlpha = subject instanceof ARGBLike argb ? argb.alpha() : 255;
            int argAlpha = argument instanceof ARGBLike argb ? argb.alpha() : 255;
            return new AlphaColor(
                    (subAlpha * argAlpha) / 255,
                    (subject.red() * argument.red()) / 255,
                    (subject.green() * argument.green()) / 255,
                    (subject.blue() * argument.blue()) / 255
            );
        }

        @java.lang.Override
        public Codec<RGBLike> argumentCodec() {
            return net.minestom.server.color.Color.STRING_CODEC;
        }
    };
    ColorModifier<ARGBLike> MULTIPLY_ARGB = new ColorModifier<>() {
        @java.lang.Override
        public RGBLike modify(RGBLike subject, ARGBLike argument) {
            int subAlpha = subject instanceof ARGBLike argb ? argb.alpha() : 255;
            int argAlpha = argument instanceof ARGBLike argb ? argb.alpha() : 255;
            return new AlphaColor(
                    (subAlpha * argAlpha) / 255,
                    (subject.red() * argument.red()) / 255,
                    (subject.green() * argument.green()) / 255,
                    (subject.blue() * argument.blue()) / 255
            );
        }

        @java.lang.Override
        public Codec<ARGBLike> argumentCodec() {
            return AlphaColor.STRING_CODEC;
        }
    };
    ColorModifier<BlendToGray> BLEND_TO_GRAY = new ColorModifier<>() {
        @java.lang.Override
        public RGBLike modify(RGBLike subject, BlendToGray argument) {
            throw new UnsupportedOperationException("blend to gray is not implemented yet");
        }

        @java.lang.Override
        public Codec<BlendToGray> argumentCodec() {
            return BlendToGray.CODEC;
        }
    };

}
