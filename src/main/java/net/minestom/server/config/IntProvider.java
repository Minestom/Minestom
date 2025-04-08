package net.minestom.server.config;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public interface IntProvider {

    @NotNull StructCodec<IntProvider> TAGGED_CODEC = Codec.RegistryTaggedUnion(
            Registries::intProviders, IntProvider::codec, "type");
    @NotNull Codec<IntProvider> CODEC = new Codec<>() {
        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable IntProvider value) {
            if (value instanceof IntProvider.Constant(int constantValue))
                return new Result.Ok<>(coder.createInt(constantValue));
            return TAGGED_CODEC.encode(coder, value);
        }

        @Override
        public @NotNull <D> Result<IntProvider> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Integer> numberResult = coder.getInt(value);
            if (numberResult instanceof Result.Ok(Integer number))
                return new Result.Ok<>(new IntProvider.Constant(number));
            return TAGGED_CODEC.decode(coder, value);
        }
    };

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends IntProvider>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends IntProvider>> registry = DynamicRegistry.create("minestom:int_provider");
        registry.register("constant", IntProvider.Constant.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("uniform", IntProvider.Uniform.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("biased_to_bottom", IntProvider.BiasedToBottom.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("clamped", IntProvider.Clamped.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("clamped_normal", IntProvider.ClampedNormal.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("weighted_list", IntProvider.WeightedList.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    int sample(Random random);

    @NotNull StructCodec<? extends IntProvider> codec();

    record Constant(int value) implements IntProvider {
        public static final StructCodec<Constant> CODEC = StructCodec.struct(
                "value", Codec.INT, Constant::value,
                Constant::new
        );

        @Override
        public int sample(Random random) {
            return value;
        }

        @Override
        public @NotNull StructCodec<Constant> codec() {
            return CODEC;
        }
    }

    record Uniform(int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<Uniform> CODEC = StructCodec.struct(
                "min_inclusive", Codec.INT, Uniform::minInclusive,
                "max_inclusive", Codec.INT, Uniform::maxInclusive,
                Uniform::new
        );

        @Override
        public int sample(Random random) {
            return random.nextInt(minInclusive, minInclusive + 1);
        }

        @Override
        public @NotNull StructCodec<Uniform> codec() {
            return CODEC;
        }
    }

    record BiasedToBottom(int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<BiasedToBottom> CODEC = StructCodec.struct(
                "min_inclusive", Codec.INT, BiasedToBottom::minInclusive,
                "max_inclusive", Codec.INT, BiasedToBottom::maxInclusive,
                BiasedToBottom::new
        );

        @Override
        public int sample(Random random) {
            return minInclusive + random.nextInt(random.nextInt(maxInclusive - minInclusive + 1) + 1);
        }

        @Override
        public @NotNull StructCodec<BiasedToBottom> codec() {
            return CODEC;
        }
    }

    record Clamped(int minInclusive, int maxInclusive, IntProvider source) implements IntProvider {
        public static final StructCodec<Clamped> CODEC = StructCodec.struct(
                "min_inclusive", Codec.INT, Clamped::minInclusive,
                "max_inclusive", Codec.INT, Clamped::maxInclusive,
                "source", IntProvider.CODEC, Clamped::source,
                Clamped::new
        );

        @Override
        public int sample(Random random) {
            return Math.min(maxInclusive, Math.max(minInclusive, source.sample(random)));
        }

        @Override
        public @NotNull StructCodec<Clamped> codec() {
            return CODEC;
        }
    }

    record ClampedNormal(float mean, float deviation, int minInclusive, int maxInclusive) implements IntProvider {
        public static final StructCodec<ClampedNormal> CODEC = StructCodec.struct(
                "mean", Codec.FLOAT, ClampedNormal::mean,
                "deviation", Codec.FLOAT, ClampedNormal::deviation,
                "min_inclusive", Codec.INT, ClampedNormal::minInclusive,
                "max_inclusive", Codec.INT, ClampedNormal::maxInclusive,
                ClampedNormal::new
        );

        @Override
        public int sample(Random random) {
            return (int) Math.min((float) maxInclusive, Math.max((float) minInclusive, (mean + (float) random.nextGaussian() * deviation)));
        }

        @Override
        public @NotNull StructCodec<ClampedNormal> codec() {
            return CODEC;
        }
    }

    record WeightedList(@NotNull List<Entry> distribution) implements IntProvider {
        public static final StructCodec<WeightedList> CODEC = StructCodec.struct(
                "distribution", Entry.CODEC.list(), WeightedList::distribution,
                WeightedList::new
        );

        @Override
        public int sample(Random random) {
            int totalWeight = 0;
            for (Entry entry : distribution) {
                totalWeight += entry.weight;
            }

            int value = random.nextInt(totalWeight);
            for (Entry entry : distribution) {
                value -= entry.weight;
                if (value < 0) {
                    return entry.data().sample(random);
                }
            }

            return distribution.getFirst().data().sample(random);
        }

        @Override
        public @NotNull StructCodec<WeightedList> codec() {
            return CODEC;
        }

        public record Entry(@NotNull IntProvider data, int weight) {
            public static final StructCodec<Entry> CODEC = StructCodec.struct(
                    "data", IntProvider.CODEC, Entry::data,
                    "weight", Codec.INT, Entry::weight,
                    Entry::new
            );
        }
    }
}
