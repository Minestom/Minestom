package net.minestom.server.config;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public interface BlockStateProvider {

    @NotNull StructCodec<BlockStateProvider> CODEC = Codec.RegistryTaggedUnion(
            Registries::blockStateProviders, BlockStateProvider::codec, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends BlockStateProvider>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends BlockStateProvider>> registry = DynamicRegistry.create("minestom:block_state_provider");
        registry.register("simple_state_provider", SimpleStateProvider.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("rotated_block_provider", RotatedBlockProvider.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("weighted_state_provider", WeightedStateProvider.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("randomized_int_state_provider", RandomizedIntStateProvider.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("noise_provider", NoiseProvider.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("dual_noise_provider", DualNoiseProvider.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("noise_threshold_provider", NoiseThresholdProvider.CODEC);
        return registry;
    }

    @NotNull Block sample(Random random, Point blockPosition);

    @NotNull StructCodec<? extends BlockStateProvider> codec();

    record SimpleStateProvider(@NotNull Block state) implements BlockStateProvider {
        public static final StructCodec<SimpleStateProvider> CODEC = StructCodec.struct(
                "state", Block.CODEC, SimpleStateProvider::state,
                SimpleStateProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            return state;
        }

        @Override
        public @NotNull StructCodec<SimpleStateProvider> codec() {
            return CODEC;
        }
    }

    record RotatedBlockProvider(@NotNull Block state) implements BlockStateProvider {
        public static final StructCodec<RotatedBlockProvider> CODEC = StructCodec.struct(
                "state", Block.CODEC, RotatedBlockProvider::state,
                RotatedBlockProvider::new
        );

        public RotatedBlockProvider(@NotNull Block state) {
            this.state = state.defaultState();
        }

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            int value = random.nextInt(3);
            return state.withProperty("axis", switch (value) {
                case 0 -> "x";
                case 1 -> "y";
                case 2 -> "z";
                default -> throw new IllegalStateException("Unexpected value: " + value);
            });
        }

        @Override
        public @NotNull StructCodec<RotatedBlockProvider> codec() {
            return CODEC;
        }
    }

    record WeightedStateProvider(@NotNull List<Entry> entries) implements BlockStateProvider {
        public static final StructCodec<WeightedStateProvider> CODEC = StructCodec.struct(
                "entries", Entry.CODEC.list(), WeightedStateProvider::entries,
                WeightedStateProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            int totalWeight = 0;
            for (Entry entry : entries) {
                totalWeight += entry.weight;
            }

            int value = random.nextInt(totalWeight);
            for (Entry entry : entries) {
                value -= entry.weight;
                if (value < 0) {
                    return entry.data();
                }
            }

            return entries.getFirst().data();
        }

        @Override
        public @NotNull StructCodec<WeightedStateProvider> codec() {
            return CODEC;
        }

        public record Entry(@NotNull Block data, int weight) {
            public static final StructCodec<Entry> CODEC = StructCodec.struct(
                    "data", Block.CODEC, Entry::data,
                    "weight", Codec.INT, Entry::weight,
                    Entry::new
            );
        }
    }

    record RandomizedIntStateProvider(
            @NotNull String property,
            @NotNull IntProvider values,
            @NotNull BlockStateProvider source
    ) implements BlockStateProvider {
        public static final StructCodec<RandomizedIntStateProvider> CODEC = StructCodec.struct(
                "property", Codec.STRING, RandomizedIntStateProvider::property,
                "values", IntProvider.CODEC, RandomizedIntStateProvider::values,
                "source", BlockStateProvider.CODEC, RandomizedIntStateProvider::source,
                RandomizedIntStateProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            Block base = source.sample(random, blockPosition);
            return base.withProperty(property, String.valueOf(values.sample(random)));
        }

        @Override
        public @NotNull StructCodec<RandomizedIntStateProvider> codec() {
            return CODEC;
        }
    }

    record NoiseProvider(
            long seed,
            @NotNull NoiseConfig noise,
            float scale,
            @NotNull List<Block> states
    ) implements BlockStateProvider {
        public static final StructCodec<NoiseProvider> CODEC = StructCodec.struct(
                "seed", Codec.LONG, NoiseProvider::seed,
                "noise", NoiseConfig.CODEC, NoiseProvider::noise,
                "scale", Codec.FLOAT, NoiseProvider::scale,
                "states", Block.CODEC.list(), NoiseProvider::states,
                NoiseProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            throw new UnsupportedOperationException("Noise provider is not implemented");
        }

        @Override
        public @NotNull StructCodec<NoiseProvider> codec() {
            return CODEC;
        }
    }

    record DualNoiseProvider(
            long seed,
            @NotNull NoiseConfig noise,
            float scale,
            @NotNull NoiseConfig slowNoise,
            float slowScale,
            @NotNull Variety variety,
            @NotNull List<Block> states
    ) implements BlockStateProvider {
        public static final StructCodec<DualNoiseProvider> CODEC = StructCodec.struct(
                "seed", Codec.LONG, DualNoiseProvider::seed,
                "noise", NoiseConfig.CODEC, DualNoiseProvider::noise,
                "scale", Codec.FLOAT, DualNoiseProvider::scale,
                "slow_noise", NoiseConfig.CODEC, DualNoiseProvider::slowNoise,
                "slow_scale", Codec.FLOAT, DualNoiseProvider::slowScale,
                "variety", Variety.CODEC, DualNoiseProvider::variety,
                "states", Block.CODEC.list(), DualNoiseProvider::states,
                DualNoiseProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            throw new UnsupportedOperationException("Dual noise provider is not implemented");
        }

        @Override
        public @NotNull StructCodec<DualNoiseProvider> codec() {
            return CODEC;
        }

        record Variety(int minInclusive, int maxInclusive) {
            public static final Codec<Variety> CODEC = StructCodec.struct(
                    "min_inclusive", Codec.INT, Variety::minInclusive,
                    "max_inclusive", Codec.INT, Variety::maxInclusive,
                    Variety::new
            ).orElse(Codec.INT.list().transform(
                    list -> new Variety(list.get(0), list.get(1)),
                    variety -> List.of(variety.minInclusive(), variety.maxInclusive())
            )).orElse(Codec.INT.transform(value -> new Variety(value, value), Variety::maxInclusive));
        }
    }

    record NoiseThresholdProvider(
            long seed,
            @NotNull NoiseConfig noise,
            float scale,
            float threshold,
            float highChance,
            @NotNull Block defaultState,
            @NotNull List<Block> lowStates,
            @NotNull List<Block> highStates
    ) implements BlockStateProvider {
        public static final StructCodec<NoiseThresholdProvider> CODEC = StructCodec.struct(
                "seed", Codec.LONG, NoiseThresholdProvider::seed,
                "noise", NoiseConfig.CODEC, NoiseThresholdProvider::noise,
                "scale", Codec.FLOAT, NoiseThresholdProvider::scale,
                "threshold", Codec.FLOAT, NoiseThresholdProvider::threshold,
                "high_chance", Codec.FLOAT, NoiseThresholdProvider::highChance,
                "default_state", Block.CODEC, NoiseThresholdProvider::defaultState,
                "low_states", Block.CODEC.list(), NoiseThresholdProvider::lowStates,
                "high_states", Block.CODEC.list(), NoiseThresholdProvider::highStates,
                NoiseThresholdProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            throw new UnsupportedOperationException("Noise threshold provider is not implemented");
        }

        @Override
        public @NotNull StructCodec<NoiseThresholdProvider> codec() {
            return CODEC;
        }
    }
}
