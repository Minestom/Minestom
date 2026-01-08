package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

public sealed interface IntProvider {
    Codec<IntProvider> CODEC = new Codec<>() {
        public static final Registry<StructCodec<? extends IntProvider>> REGISTRY = DynamicRegistry.fromMap(RegistryKey.unsafeOf("int_provider"),
                Map.entry(Key.key("uniform"), Uniform.CODEC));
        private static final StructCodec<IntProvider> TAGGED_CODEC = Codec.RegistryTaggedUnion(REGISTRY, IntProvider::codec);

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable IntProvider value) {
            if (value instanceof IntProvider.Constant(int number))
                return new Result.Ok<>(coder.createInt(number));
            return TAGGED_CODEC.encode(coder, value);
        }

        @Override
        public <D> Result<IntProvider> decode(Transcoder<D> coder, D value) {
            final Result<Integer> numberResult = coder.getInt(value);
            if (numberResult instanceof Result.Ok(Integer number))
                return new Result.Ok<>(new IntProvider.Constant(number));
            return TAGGED_CODEC.decode(coder, value);
        }
    };

    record Constant(int value) implements IntProvider {
        public static final Codec<Constant> CODEC = Codec.INT.transform(Constant::new, Constant::value);

        @Override
        public int sample(Random random) {
            return value;
        }

        @Override
        public StructCodec<? extends IntProvider> codec() {
            throw new UnsupportedOperationException("Constant values are serialized as a special case, use IntProvider#CODEC");
        }
    }

    record Uniform(int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<Uniform> CODEC = StructCodec.struct(
                "min_inclusive", Codec.INT, Uniform::minInclusive,
                "max_inclusive", Codec.INT, Uniform::maxInclusive,
                Uniform::new);

        @Override
        public int sample(Random random) {
            return random.nextInt(minInclusive, maxInclusive + 1);
        }

        @Override
        public StructCodec<? extends IntProvider> codec() {
            return CODEC;
        }
    }

    record BiasedToBottom(int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<BiasedToBottom> CODEC = StructCodec.struct(
                "min_inclusive", Codec.INT, BiasedToBottom::minInclusive,
                "max_inclusive", Codec.INT, BiasedToBottom::maxInclusive,
                BiasedToBottom::new);

        @Override
        public int sample(Random random) {
            return minInclusive + random.nextInt(random.nextInt(maxInclusive - minInclusive + 1) + 1);
        }

        @Override
        public StructCodec<? extends IntProvider> codec() {
            return CODEC;
        }
    }

    record Clamped(IntProvider source, int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<Clamped> CODEC = StructCodec.struct(
                "source", IntProvider.CODEC, Clamped::source,
                "min_inclusive", Codec.INT, Clamped::minInclusive,
                "max_inclusive", Codec.INT, Clamped::maxInclusive,
                Clamped::new);

        @Override
        public int sample(Random random) {
            return Math.clamp(source.sample(random), minInclusive, maxInclusive);
        }

        @Override
        public StructCodec<? extends IntProvider> codec() {
            return CODEC;
        }
    }

    record Weighted(WeightedList<IntProvider> distribution) implements IntProvider {
        public static final StructCodec<Weighted> CODEC = StructCodec.struct(
                "distribution", WeightedList.codec(IntProvider.CODEC), Weighted::distribution,
                Weighted::new);

        @Override
        public int sample(Random random) {
            return distribution.pickOrThrow(random).sample(random);
        }

        @Override
        public StructCodec<? extends IntProvider> codec() {
            return CODEC;
        }
    }

    record ClampedNormal(double mean, double deviation, int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<ClampedNormal> CODEC = StructCodec.struct(
                "mean", Codec.DOUBLE, ClampedNormal::mean,
                "deviation", Codec.DOUBLE, ClampedNormal::deviation,
                "min_inclusive", Codec.INT, ClampedNormal::minInclusive,
                "max_inclusive", Codec.INT, ClampedNormal::maxInclusive,
                ClampedNormal::new);

        @Override
        public int sample(Random random) {
            return Math.clamp((int) (mean + random.nextGaussian() * deviation), minInclusive, maxInclusive);
        }

        @Override
        public StructCodec<? extends IntProvider> codec() {
            return CODEC;
        }
    }

    int sample(Random random);

    StructCodec<? extends IntProvider> codec();
}
