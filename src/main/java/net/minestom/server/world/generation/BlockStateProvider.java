package net.minestom.server.world.generation;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.IntProvider;
import net.minestom.server.utils.Range;
import net.minestom.server.utils.WeightedList;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Chooses the block state to place at a position.
 * <p>
 * Providers live in the {@code worldgen/block_state_provider} registry, and wherever one is expected it may be
 * given by key or written inline. The inline form is either a block state map, which places that state, or a
 * map with a {@code type} field naming one of the providers below.
 */
public sealed interface BlockStateProvider extends Holder.Direct<BlockStateProvider>, BlockStateProviders {
    Registry<StructCodec<? extends BlockStateProvider>> REGISTRY = DynamicRegistry.fromMap(Key.key("block_state_provider_type"),
            Map.entry(Key.key("simple"), Simple.CODEC),
            Map.entry(Key.key("copy_properties"), CopyProperties.CODEC),
            Map.entry(Key.key("rule_based"), RuleBased.CODEC),
            Map.entry(Key.key("weighted"), Weighted.CODEC),
            Map.entry(Key.key("random_block"), RandomBlock.CODEC),
            Map.entry(Key.key("rotated"), RotatedBlock.CODEC),
            Map.entry(Key.key("randomized_int"), RandomizedIntState.CODEC),
            Map.entry(Key.key("noise"), Noise.CODEC),
            Map.entry(Key.key("noise_threshold"), NoiseThreshold.CODEC),
            Map.entry(Key.key("dual_noise"), DualNoise.CODEC)
    );

    StructCodec<BlockStateProvider> TYPED_CODEC = Codec.RegistryTaggedUnion(REGISTRY, BlockStateProvider::codec);

    Codec<BlockStateProvider> REGISTRY_CODEC = Codec.Either(Block.STATE_STRUCT_CODEC, TYPED_CODEC).transform(
            either -> either.unify(Simple::new, provider -> provider),
            provider -> provider instanceof Simple(Block state) ? Either.left(state) : Either.right(provider));

    Codec<Holder<BlockStateProvider>> CODEC = Holder.codec(Registries::blockStateProvider, REGISTRY_CODEC);

    /**
     * <p>Creates a new registry for block state providers, loading the vanilla providers.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<BlockStateProvider> createDefaultRegistry(Registries registries) {
        return DynamicRegistry.create(BuiltinRegistries.BLOCK_STATE_PROVIDER, REGISTRY_CODEC,
                registries, (delegate, registry) -> new Registries.Delegating() {
                    @Override
                    public Registries registries() {
                        return delegate;
                    }

                    @Override
                    public DynamicRegistry<BlockStateProvider> blockStateProvider() {
                        return registry;
                    }
                });
    }

    StructCodec<? extends BlockStateProvider> codec();

    /**
     * Always places the same state.
     *
     * @param state the state to place
     */
    record Simple(Block state) implements BlockStateProvider {
        static final StructCodec<Simple> CODEC = StructCodec.struct(
                "state", Block.COMPACT_STATE_CODEC, Simple::state,
                Simple::new);

        @Override
        public StructCodec<Simple> codec() {
            return CODEC;
        }
    }

    /**
     * Places the state of the source provider, carrying over the properties of the block already there.
     *
     * @param source the provider of the state to place
     */
    record CopyProperties(Holder<BlockStateProvider> source) implements BlockStateProvider {
        static final StructCodec<CopyProperties> CODEC = StructCodec.struct(
                "source", Codec.ForwardRef(() -> BlockStateProvider.CODEC), CopyProperties::source,
                CopyProperties::new);

        @Override
        public StructCodec<CopyProperties> codec() {
            return CODEC;
        }
    }

    /**
     * Places the state of the first rule whose predicate matches.
     *
     * @param fallback the provider used when no rule matches, null to place nothing
     * @param rules    the rules in the order they are tried
     */
    record RuleBased(@Nullable Holder<BlockStateProvider> fallback, List<Rule> rules) implements BlockStateProvider {
        private static final int MAX_ENTRIES = 256;

        static final StructCodec<RuleBased> CODEC = StructCodec.struct(
                "fallback", Codec.ForwardRef(() -> BlockStateProvider.CODEC).optional(), RuleBased::fallback,
                "rules", Rule.CODEC.list(MAX_ENTRIES), RuleBased::rules,
                RuleBased::new);

        public RuleBased {
            rules = List.copyOf(rules);
            Check.argCondition(rules.size() > MAX_ENTRIES, "At most " + MAX_ENTRIES + " rules are allowed");
        }

        @Override
        public StructCodec<RuleBased> codec() {
            return CODEC;
        }

        /**
         * One branch of a rule based provider.
         *
         * @param ifTrue the predicate the position must satisfy
         * @param then   the provider used when the predicate matches
         */
        public record Rule(PositionPredicate ifTrue, Holder<BlockStateProvider> then) {
            static final Codec<Rule> CODEC = StructCodec.struct(
                    "if_true", PositionPredicate.CODEC, Rule::ifTrue,
                    "then", Codec.ForwardRef(() -> BlockStateProvider.CODEC), Rule::then,
                    Rule::new);
        }
    }

    /**
     * Picks a state at random, weighted against the others.
     *
     * @param entries the candidate states, must not be empty
     */
    record Weighted(WeightedList<Block> entries) implements BlockStateProvider {
        static final StructCodec<Weighted> CODEC = StructCodec.struct(
                "entries", WeightedList.codec(Block.COMPACT_STATE_CODEC), Weighted::entries,
                Weighted::new);

        public Weighted {
            Check.argCondition(entries.entries().isEmpty(), "Entries must not be empty");
        }

        @Override
        public StructCodec<Weighted> codec() {
            return CODEC;
        }
    }

    /**
     * Picks the default state of a block of the set, at random and evenly.
     *
     * @param blocks the candidate blocks
     */
    record RandomBlock(RegistryTag<Block> blocks) implements BlockStateProvider {
        static final StructCodec<RandomBlock> CODEC = StructCodec.struct(
                "blocks", RegistryTag.codec(Registries::blocks), RandomBlock::blocks,
                RandomBlock::new);

        @Override
        public StructCodec<RandomBlock> codec() {
            return CODEC;
        }
    }

    /**
     * Places the state of the source provider turned to face a direction.
     *
     * @param state     the provider of the state to place
     * @param direction the direction to face, null to pick one at random
     */
    record RotatedBlock(Holder<BlockStateProvider> state, @Nullable Direction direction) implements BlockStateProvider {
        static final StructCodec<RotatedBlock> CODEC = StructCodec.struct(
                "state", Codec.ForwardRef(() -> BlockStateProvider.CODEC), RotatedBlock::state,
                "direction", Direction.CODEC.optional(), RotatedBlock::direction,
                RotatedBlock::new);

        @Override
        public StructCodec<RotatedBlock> codec() {
            return CODEC;
        }
    }

    /**
     * Places the state of the source provider with one integer property rolled at random.
     *
     * @param source   the provider of the state to place
     * @param property the name of the property to roll
     * @param values   the values the property is rolled from
     */
    record RandomizedIntState(Holder<BlockStateProvider> source, String property, IntProvider values) implements BlockStateProvider {
        static final StructCodec<RandomizedIntState> CODEC = StructCodec.struct(
                "source", Codec.ForwardRef(() -> BlockStateProvider.CODEC), RandomizedIntState::source,
                "property", Codec.STRING, RandomizedIntState::property,
                "values", IntProvider.CODEC, RandomizedIntState::values,
                RandomizedIntState::new);

        @Override
        public StructCodec<RandomizedIntState> codec() {
            return CODEC;
        }
    }

    /**
     * Picks a state by sampling a noise at the position.
     *
     * @param seed   the seed the noise is sampled with
     * @param noise  the noise to sample
     * @param scale  how far the sampled position is scaled, must be positive
     * @param states the candidate states, must not be empty
     */
    record Noise(long seed, NormalNoise noise, float scale, List<Block> states) implements BlockStateProvider {
        private static final int MAX_ENTRIES = 256;

        static final StructCodec<Noise> CODEC = StructCodec.struct(
                "seed", Codec.LONG, Noise::seed,
                "noise", NormalNoise.CODEC, Noise::noise,
                "scale", Codec.FLOAT, Noise::scale,
                "states", Block.COMPACT_STATE_CODEC.list(MAX_ENTRIES), Noise::states,
                Noise::new);

        public Noise {
            states = List.copyOf(states);
            Check.argCondition(scale <= 0, "Scale must be positive");
            Check.argCondition(states.isEmpty() || states.size() > MAX_ENTRIES, "States must hold between 1 and " + MAX_ENTRIES + " entries");
        }

        @Override
        public StructCodec<Noise> codec() {
            return CODEC;
        }
    }

    /**
     * Picks a state from one of two sets depending on how the sampled noise compares to a threshold.
     *
     * @param seed         the seed the noise is sampled with
     * @param noise        the noise to sample
     * @param scale        how far the sampled position is scaled, must be positive
     * @param threshold    the sample value the two sets are split at, between -1 and 1
     * @param highChance   how often a sample above the threshold picks from the high states, between 0 and 1
     * @param defaultState the state placed when the high roll fails
     * @param lowStates    the candidates below the threshold, must not be empty
     * @param highStates   the candidates above the threshold, must not be empty
     */
    record NoiseThreshold(
            long seed, NormalNoise noise, float scale,
            float threshold, float highChance,
            Block defaultState, List<Block> lowStates, List<Block> highStates
    ) implements BlockStateProvider {
        private static final int MAX_ENTRIES = 256;

        static final StructCodec<NoiseThreshold> CODEC = StructCodec.struct(
                "seed", Codec.LONG, NoiseThreshold::seed,
                "noise", NormalNoise.CODEC, NoiseThreshold::noise,
                "scale", Codec.FLOAT, NoiseThreshold::scale,
                "threshold", Codec.FLOAT, NoiseThreshold::threshold,
                "high_chance", Codec.FLOAT, NoiseThreshold::highChance,
                "default_state", Block.COMPACT_STATE_CODEC, NoiseThreshold::defaultState,
                "low_states", Block.COMPACT_STATE_CODEC.list(MAX_ENTRIES), NoiseThreshold::lowStates,
                "high_states", Block.COMPACT_STATE_CODEC.list(MAX_ENTRIES), NoiseThreshold::highStates,
                NoiseThreshold::new);

        public NoiseThreshold {
            lowStates = List.copyOf(lowStates);
            highStates = List.copyOf(highStates);
            Check.argCondition(scale <= 0, "Scale must be positive");
            Check.argCondition(threshold < -1 || threshold > 1, "Threshold must be between -1 and 1");
            Check.argCondition(highChance < 0 || highChance > 1, "High chance must be between 0 and 1");
            Check.argCondition(lowStates.isEmpty() || lowStates.size() > MAX_ENTRIES, "Low states must hold between 1 and " + MAX_ENTRIES + " entries");
            Check.argCondition(highStates.isEmpty() || highStates.size() > MAX_ENTRIES, "High states must hold between 1 and " + MAX_ENTRIES + " entries");
        }

        @Override
        public StructCodec<NoiseThreshold> codec() {
            return CODEC;
        }
    }

    /**
     * Picks a state by sampling a fast noise for the position and a slow noise for the region around it.
     *
     * @param variety   how many of the states one region may draw from, both bounds set and between 1 and 64
     * @param slowNoise the noise sampled per region
     * @param slowScale how far the region position is scaled, must be positive
     * @param seed      the seed the fast noise is sampled with
     * @param noise     the noise sampled per position
     * @param scale     how far the sampled position is scaled, must be positive
     * @param states    the candidate states, must not be empty
     */
    record DualNoise(
            Range.Int variety, NormalNoise slowNoise, float slowScale,
            long seed, NormalNoise noise, float scale, List<Block> states
    ) implements BlockStateProvider {
        private static final int MAX_ENTRIES = 256;

        private static final Codec<Range.Int> BOUNDS_CODEC = StructCodec.struct(
                "min_inclusive", Codec.INT, Range.Int::min,
                "max_inclusive", Codec.INT, Range.Int::max,
                Range.Int::new);
        static final Codec<Range.Int> VARIETY_CODEC = Codec.Either(Codec.INT, Codec.Either(Codec.INT.list(2), BOUNDS_CODEC)).transform(
                either -> either.unify(Range.Int::new, bounds -> bounds.unify(DualNoise::varietyOf, range -> range)),
                range -> range.min().equals(range.max()) ? Either.left(range.min()) : Either.right(Either.left(List.of(range.min(), range.max()))));
        static final StructCodec<DualNoise> CODEC = StructCodec.struct(
                "variety", VARIETY_CODEC, DualNoise::variety,
                "slow_noise", NormalNoise.CODEC, DualNoise::slowNoise,
                "slow_scale", Codec.FLOAT, DualNoise::slowScale,
                "seed", Codec.LONG, DualNoise::seed,
                "noise", NormalNoise.CODEC, DualNoise::noise,
                "scale", Codec.FLOAT, DualNoise::scale,
                "states", Block.COMPACT_STATE_CODEC.list(MAX_ENTRIES), DualNoise::states,
                DualNoise::new);

        public DualNoise {
            states = List.copyOf(states);
            Check.argCondition(variety.min() == null || variety.max() == null || variety.min() < 1 || variety.max() > 64,
                    "Variety must have both bounds set, between 1 and 64");
            Check.argCondition(slowScale <= 0, "Slow scale must be positive");
            Check.argCondition(scale <= 0, "Scale must be positive");
            Check.argCondition(states.isEmpty() || states.size() > MAX_ENTRIES, "States must hold between 1 and " + MAX_ENTRIES + " entries");
        }

        private static Range.Int varietyOf(List<Integer> bounds) {
            if (bounds.size() != 2) throw new IllegalArgumentException("variety needs exactly two bounds, got " + bounds);
            return new Range.Int(bounds.getFirst(), bounds.getLast());
        }

        @Override
        public StructCodec<DualNoise> codec() {
            return CODEC;
        }
    }
}
