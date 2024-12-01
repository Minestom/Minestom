package net.minestom.server.config;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public interface BlockStateProvider {

    @NotNull BinaryTagSerializer<BlockStateProvider> NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::blockStateProviders, BlockStateProvider::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends BlockStateProvider>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends BlockStateProvider>> registry = DynamicRegistry.create("minestom:block_state_provider");
        registry.register("simple_state_provider", SimpleStateProvider.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("rotated_block_provider", RotatedBlockProvider.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("weighted_state_provider", WeightedStateProvider.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("randomized_int_state_provider", RandomizedIntStateProvider.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("noise_provider", NoiseProvider.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("dual_noise_provider", DualNoiseProvider.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("noise_threshold_provider", NoiseThresholdProvider.NBT_TYPE);
        return registry;
    }

    @NotNull Block sample(Random random, Point blockPosition);

    @NotNull BinaryTagSerializer<? extends BlockStateProvider> nbtType();

    record SimpleStateProvider(@NotNull Block state) implements BlockStateProvider {
        public static final BinaryTagSerializer<SimpleStateProvider> NBT_TYPE = BinaryTagSerializer.object(
                "state", Block.NBT_TYPE, SimpleStateProvider::state,
                SimpleStateProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            return state;
        }

        @Override
        public @NotNull BinaryTagSerializer<SimpleStateProvider> nbtType() {
            return NBT_TYPE;
        }
    }

    record RotatedBlockProvider(@NotNull Block state) implements BlockStateProvider {
        public static final BinaryTagSerializer<RotatedBlockProvider> NBT_TYPE = BinaryTagSerializer.object(
                "state", Block.NBT_TYPE, RotatedBlockProvider::state,
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
        public @NotNull BinaryTagSerializer<RotatedBlockProvider> nbtType() {
            return NBT_TYPE;
        }
    }

    record WeightedStateProvider(@NotNull List<Entry> entries) implements BlockStateProvider {
        public static final BinaryTagSerializer<WeightedStateProvider> NBT_TYPE = BinaryTagSerializer.object(
                "entries", Entry.NBT_TYPE.list(), WeightedStateProvider::entries,
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
        public @NotNull BinaryTagSerializer<WeightedStateProvider> nbtType() {
            return NBT_TYPE;
        }

        public record Entry(@NotNull Block data, int weight) {
            public static final BinaryTagSerializer<Entry> NBT_TYPE = BinaryTagSerializer.object(
                    "data", Block.NBT_TYPE, Entry::data,
                    "weight", BinaryTagSerializer.INT, Entry::weight,
                    Entry::new
            );
        }
    }

    record RandomizedIntStateProvider(
            @NotNull String property,
            @NotNull IntProvider values,
            @NotNull BlockStateProvider source
    ) implements BlockStateProvider {
        public static final BinaryTagSerializer<RandomizedIntStateProvider> NBT_TYPE = BinaryTagSerializer.object(
                "property", BinaryTagSerializer.STRING, RandomizedIntStateProvider::property,
                "values", IntProvider.NBT_TYPE, RandomizedIntStateProvider::values,
                "source", BlockStateProvider.NBT_TYPE, RandomizedIntStateProvider::source,
                RandomizedIntStateProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            Block base = source.sample(random, blockPosition);
            return base.withProperty(property, String.valueOf(values.sample(random)));
        }

        @Override
        public @NotNull BinaryTagSerializer<RandomizedIntStateProvider> nbtType() {
            return NBT_TYPE;
        }
    }

    record NoiseProvider(
            long seed,
            @NotNull NoiseConfig noise,
            float scale,
            @NotNull List<Block> states
    ) implements BlockStateProvider {
        public static final BinaryTagSerializer<NoiseProvider> NBT_TYPE = BinaryTagSerializer.object(
                "seed", BinaryTagSerializer.LONG, NoiseProvider::seed,
                "noise", NoiseConfig.NBT_TYPE, NoiseProvider::noise,
                "scale", BinaryTagSerializer.FLOAT, NoiseProvider::scale,
                "states", Block.NBT_TYPE.list(), NoiseProvider::states,
                NoiseProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            throw new UnsupportedOperationException("Noise provider is not implemented");
        }

        @Override
        public @NotNull BinaryTagSerializer<NoiseProvider> nbtType() {
            return NBT_TYPE;
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
        public static final BinaryTagSerializer<DualNoiseProvider> NBT_TYPE = BinaryTagSerializer.object(
                "seed", BinaryTagSerializer.LONG, DualNoiseProvider::seed,
                "noise", NoiseConfig.NBT_TYPE, DualNoiseProvider::noise,
                "scale", BinaryTagSerializer.FLOAT, DualNoiseProvider::scale,
                "slow_noise", NoiseConfig.NBT_TYPE, DualNoiseProvider::slowNoise,
                "slow_scale", BinaryTagSerializer.FLOAT, DualNoiseProvider::slowScale,
                "variety", Variety.NBT_TYPE, DualNoiseProvider::variety,
                "states", Block.NBT_TYPE.list(), DualNoiseProvider::states,
                DualNoiseProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            throw new UnsupportedOperationException("Dual noise provider is not implemented");
        }

        @Override
        public @NotNull BinaryTagSerializer<DualNoiseProvider> nbtType() {
            return NBT_TYPE;
        }

        record Variety(int minInclusive, int maxInclusive) {
            public static final BinaryTagSerializer<Variety> NBT_TYPE = new BinaryTagSerializer<>() {
                @Override
                public @NotNull BinaryTag write(@NotNull Context context, @NotNull Variety value) {
                    return CompoundBinaryTag.builder()
                            .putInt("min_inclusive", value.minInclusive())
                            .putInt("max_inclusive", value.maxInclusive())
                            .build();
                }

                @Override
                public @NotNull Variety read(@NotNull Context context, @NotNull BinaryTag tag) {
                    return switch (tag) {
                        case CompoundBinaryTag compound ->
                                new Variety(compound.getInt("min_inclusive"), compound.getInt("max_inclusive"));
                        case ListBinaryTag list -> new Variety(list.getInt(0), list.getInt(1));
                        case NumberBinaryTag number -> new Variety(number.intValue(), number.intValue());
                        default -> throw new IllegalArgumentException("Expected compound, list or number for variety");
                    };
                }
            };
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
        public static final BinaryTagSerializer<NoiseThresholdProvider> NBT_TYPE = BinaryTagSerializer.object(
                "seed", BinaryTagSerializer.LONG, NoiseThresholdProvider::seed,
                "noise", NoiseConfig.NBT_TYPE, NoiseThresholdProvider::noise,
                "scale", BinaryTagSerializer.FLOAT, NoiseThresholdProvider::scale,
                "threshold", BinaryTagSerializer.FLOAT, NoiseThresholdProvider::threshold,
                "high_chance", BinaryTagSerializer.FLOAT, NoiseThresholdProvider::highChance,
                "default_state", Block.NBT_TYPE, NoiseThresholdProvider::defaultState,
                "low_states", Block.NBT_TYPE.list(), NoiseThresholdProvider::lowStates,
                "high_states", Block.NBT_TYPE.list(), NoiseThresholdProvider::highStates,
                NoiseThresholdProvider::new
        );

        @Override
        public @NotNull Block sample(Random random, Point blockPosition) {
            throw new UnsupportedOperationException("Noise threshold provider is not implemented");
        }

        @Override
        public @NotNull BinaryTagSerializer<NoiseThresholdProvider> nbtType() {
            return NBT_TYPE;
        }
    }
}
