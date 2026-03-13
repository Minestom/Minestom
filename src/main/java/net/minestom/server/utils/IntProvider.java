package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public sealed interface IntProvider {
    Registry<StructCodec<? extends IntProvider>> REGISTRY = DynamicRegistry.fromMap(Key.key("int_provider"),
            Map.entry(Key.key("constant"), Constant.CODEC),
            Map.entry(Key.key("uniform"), Uniform.CODEC),
            Map.entry(Key.key("biased_to_bottom"), BiasedToBottom.CODEC),
            Map.entry(Key.key("clamped"), Clamped.CODEC),
            Map.entry(Key.key("weighted_list"), Weighted.CODEC),
            Map.entry(Key.key("clamped_normal"), ClampedNormal.CODEC),
            Map.entry(Key.key("trapezoid"), Trapezoid.CODEC)
    );
    StructCodec<IntProvider> REGISTRY_CODEC = Codec.RegistryTaggedUnion(REGISTRY, IntProvider::codec);

    Codec<IntProvider> CODEC = Codec.Either(Codec.INT, REGISTRY_CODEC).transform(
            it -> it.unify(Constant::new, Function.identity()),
            it -> it instanceof Constant(int value) ? Either.left(value) : Either.right(it)
    );

    record Constant(int value) implements IntProvider {
        public static final StructCodec<Constant> CODEC = StructCodec.struct(
                "value", Codec.INT, Constant::value,
                Constant::new
        );

        @Override
        public int minInclusive() {
            return value;
        }

        @Override
        public int maxInclusive() {
            return value;
        }

        @Override
        public int sample(Random ignored) {
            return value;
        }

        @Override
        public StructCodec<Constant> codec() {
            return CODEC;
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
        public StructCodec<Uniform> codec() {
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
        public StructCodec<BiasedToBottom> codec() {
            return CODEC;
        }
    }

    record Clamped(IntProvider source, int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<Clamped> CODEC = StructCodec.struct(
                "source", Codec.ForwardRef(() -> IntProvider.CODEC), Clamped::source,
                "min_inclusive", Codec.INT, Clamped::minInclusive,
                "max_inclusive", Codec.INT, Clamped::maxInclusive,
                Clamped::new);

        @Override
        public int sample(Random random) {
            return Math.clamp(source.sample(random), minInclusive, maxInclusive);
        }

        @Override
        public StructCodec<Clamped> codec() {
            return CODEC;
        }
    }

    record Weighted(WeightedList<IntProvider> distribution) implements IntProvider {
        public static final StructCodec<Weighted> CODEC = StructCodec.struct(
                "distribution", WeightedList.codec(Codec.ForwardRef(() -> IntProvider.CODEC)), Weighted::distribution,
                Weighted::new);

        @Override
        public int minInclusive() {
            return 0;
        }

        @Override
        public int maxInclusive() {
            return 0;
        }

        @Override
        public int sample(Random random) {
            return distribution.pickOrThrow(random).sample(random);
        }

        @Override
        public StructCodec<Weighted> codec() {
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
        public StructCodec<ClampedNormal> codec() {
            return CODEC;
        }
    }

    record Trapezoid(int minInclusive, int maxInclusive, int plateau) implements IntProvider {
        public static final StructCodec<Trapezoid> CODEC = StructCodec.struct(
                "min", Codec.INT, Trapezoid::minInclusive,
                "max", Codec.INT, Trapezoid::maxInclusive,
                "plateau", Codec.INT, Trapezoid::plateau,
                Trapezoid::new);

        @Override
        public int sample(Random random) {
            if (plateau == 0 && maxInclusive == -minInclusive) {
                return random.nextInt(maxInclusive + 1) - random.nextInt(maxInclusive + 1);
            }
            int range = maxInclusive - minInclusive;
            if (plateau == range) {
                return random.nextInt(minInclusive, maxInclusive + 1);
            } else {
                int plateauStart = (range - plateau) / 2;
                int plateauEnd = range - plateauStart;
                return minInclusive + random.nextInt(plateauEnd + 1) + random.nextInt(plateauStart + 1);
            }
        }

        @Override
        public StructCodec<Trapezoid> codec() {
            return CODEC;
        }
    }

    int minInclusive();

    int maxInclusive();

    int sample(Random random);

    StructCodec<? extends IntProvider> codec();
}
