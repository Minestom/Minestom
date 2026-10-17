package net.minestom.server.world.generation;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.fluid.Fluid;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biome.Biome;

import java.util.List;
import java.util.Map;

/**
 * A test applied to a position in the world, offset from the position being transformed. Unlike
 * {@link net.minestom.server.instance.block.predicate.BlockPredicate}, which matches a block value, this
 * reads the surrounding world, so several of its cases never look at a block at all.
 */
public sealed interface PositionPredicate {
    Registry<StructCodec<? extends PositionPredicate>> REGISTRY = DynamicRegistry.fromMap(Key.key("block_predicate_type"),
            Map.entry(Key.key("matching_blocks"), MatchingBlocks.CODEC),
            Map.entry(Key.key("matching_block_tag"), MatchingBlockTag.CODEC),
            Map.entry(Key.key("matching_fluids"), MatchingFluids.CODEC),
            Map.entry(Key.key("matching_biomes"), MatchingBiomes.CODEC),
            Map.entry(Key.key("has_sturdy_face"), HasSturdyFace.CODEC),
            Map.entry(Key.key("solid"), Solid.CODEC),
            Map.entry(Key.key("replaceable"), Replaceable.CODEC),
            Map.entry(Key.key("would_survive"), WouldSurvive.CODEC),
            Map.entry(Key.key("inside_world_bounds"), InsideWorldBounds.CODEC),
            Map.entry(Key.key("any_of"), AnyOf.CODEC),
            Map.entry(Key.key("all_of"), AllOf.CODEC),
            Map.entry(Key.key("not"), Not.CODEC),
            Map.entry(Key.key("true"), True.CODEC),
            Map.entry(Key.key("unobstructed"), Unobstructed.CODEC),
            Map.entry(Key.key("height_range"), HeightRange.CODEC),
            Map.entry(Key.key("volume_match"), VolumeMatch.CODEC)
    );
    StructCodec<PositionPredicate> CODEC = Codec.RegistryTaggedUnion(REGISTRY, PositionPredicate::codec);

    StructCodec<? extends PositionPredicate> codec();

    private static void checkOffset(Point offset) {
        Check.argCondition(Math.abs(offset.blockX()) > 16 || Math.abs(offset.blockY()) > 16 || Math.abs(offset.blockZ()) > 16,
                "Offset must be at most 16 blocks on each axis");
    }

    /**
     * Matches one of a set of blocks.
     *
     * @param offset the position to test, relative to the position being transformed, at most 16 blocks on each axis
     * @param blocks the blocks that satisfy the test
     */
    record MatchingBlocks(Point offset, RegistryTag<Block> blocks) implements PositionPredicate {
        static final StructCodec<MatchingBlocks> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), MatchingBlocks::offset,
                "blocks", RegistryTag.codec(Registries::blocks), MatchingBlocks::blocks,
                MatchingBlocks::new);

        public MatchingBlocks {
            checkOffset(offset);
        }

        @Override
        public StructCodec<MatchingBlocks> codec() {
            return CODEC;
        }
    }

    /**
     * Matches any block of a tag.
     *
     * @param offset the position to test, relative to the position being transformed, at most 16 blocks on each axis
     * @param tag    the block tag that satisfies the test
     */
    record MatchingBlockTag(Point offset, Key tag) implements PositionPredicate {
        static final StructCodec<MatchingBlockTag> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), MatchingBlockTag::offset,
                "tag", Codec.KEY, MatchingBlockTag::tag,
                MatchingBlockTag::new);

        public MatchingBlockTag {
            checkOffset(offset);
        }

        @Override
        public StructCodec<MatchingBlockTag> codec() {
            return CODEC;
        }
    }

    /**
     * Matches one of a set of fluids.
     *
     * @param offset the position to test, relative to the position being transformed, at most 16 blocks on each axis
     * @param fluids the fluids that satisfy the test
     */
    record MatchingFluids(Point offset, RegistryTag<Fluid> fluids) implements PositionPredicate {
        static final StructCodec<MatchingFluids> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), MatchingFluids::offset,
                "fluids", RegistryTag.codec(Registries::fluid), MatchingFluids::fluids,
                MatchingFluids::new);

        public MatchingFluids {
            checkOffset(offset);
        }

        @Override
        public StructCodec<MatchingFluids> codec() {
            return CODEC;
        }
    }

    /**
     * Matches one of a set of biomes.
     *
     * @param biomes the biomes that satisfy the test
     */
    record MatchingBiomes(RegistryTag<Biome> biomes) implements PositionPredicate {
        static final StructCodec<MatchingBiomes> CODEC = StructCodec.struct(
                "biomes", RegistryTag.codec(Registries::biome), MatchingBiomes::biomes,
                MatchingBiomes::new);

        @Override
        public StructCodec<MatchingBiomes> codec() {
            return CODEC;
        }
    }

    /**
     * Matches a block whose given face can support another block.
     *
     * @param offset    the position to test, relative to the position being transformed, at most 16 blocks on each axis
     * @param direction the face that must be sturdy enough to support a block
     */
    record HasSturdyFace(Point offset, Direction direction) implements PositionPredicate {
        static final StructCodec<HasSturdyFace> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), HasSturdyFace::offset,
                "direction", Direction.CODEC, HasSturdyFace::direction,
                HasSturdyFace::new);

        public HasSturdyFace {
            checkOffset(offset);
        }

        @Override
        public StructCodec<HasSturdyFace> codec() {
            return CODEC;
        }
    }

    /**
     * Matches a block with a solid collision shape.
     *
     * @param offset the position to test, relative to the position being transformed, at most 16 blocks on each axis
     */
    record Solid(Point offset) implements PositionPredicate {
        static final StructCodec<Solid> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), Solid::offset,
                Solid::new);

        public Solid {
            checkOffset(offset);
        }

        @Override
        public StructCodec<Solid> codec() {
            return CODEC;
        }
    }

    /**
     * Matches a block that world generation is allowed to overwrite.
     *
     * @param offset the position to test, relative to the position being transformed, at most 16 blocks on each axis
     */
    record Replaceable(Point offset) implements PositionPredicate {
        static final StructCodec<Replaceable> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), Replaceable::offset,
                Replaceable::new);

        public Replaceable {
            checkOffset(offset);
        }

        @Override
        public StructCodec<Replaceable> codec() {
            return CODEC;
        }
    }

    /**
     * Matches a position the given state could stay at.
     *
     * @param offset the position to test, relative to the position being transformed, at most 16 blocks on each axis
     * @param state  the state that must be able to stay at the tested position
     */
    record WouldSurvive(Point offset, Block state) implements PositionPredicate {
        static final StructCodec<WouldSurvive> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), WouldSurvive::offset,
                "state", Block.COMPACT_STATE_CODEC, WouldSurvive::state,
                WouldSurvive::new);

        public WouldSurvive {
            checkOffset(offset);
        }

        @Override
        public StructCodec<WouldSurvive> codec() {
            return CODEC;
        }
    }

    /**
     * Matches a position within the buildable height of the world.
     *
     * @param offset the position to test, relative to the position being transformed, at most 16 blocks on each axis
     */
    record InsideWorldBounds(Point offset) implements PositionPredicate {
        static final StructCodec<InsideWorldBounds> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), InsideWorldBounds::offset,
                InsideWorldBounds::new);

        public InsideWorldBounds {
            checkOffset(offset);
        }

        @Override
        public StructCodec<InsideWorldBounds> codec() {
            return CODEC;
        }
    }

    /**
     * Matches when at least one of the given predicates matches. Matches nothing when the list is empty.
     *
     * @param predicates the predicates to try
     */
    record AnyOf(List<PositionPredicate> predicates) implements PositionPredicate {
        private static final int MAX_PREDICATES = 256;

        static final StructCodec<AnyOf> CODEC = StructCodec.struct(
                "predicates", Codec.ForwardRef(() -> PositionPredicate.CODEC).list(MAX_PREDICATES), AnyOf::predicates,
                AnyOf::new);

        public AnyOf {
            predicates = List.copyOf(predicates);
            Check.argCondition(predicates.size() > MAX_PREDICATES, "At most " + MAX_PREDICATES + " predicates are allowed");
        }

        @Override
        public StructCodec<AnyOf> codec() {
            return CODEC;
        }
    }

    /**
     * Matches when every one of the given predicates matches. Matches everything when the list is empty.
     *
     * @param predicates the predicates that must all match
     */
    record AllOf(List<PositionPredicate> predicates) implements PositionPredicate {
        private static final int MAX_PREDICATES = 256;

        static final StructCodec<AllOf> CODEC = StructCodec.struct(
                "predicates", Codec.ForwardRef(() -> PositionPredicate.CODEC).list(MAX_PREDICATES), AllOf::predicates,
                AllOf::new);

        public AllOf {
            predicates = List.copyOf(predicates);
            Check.argCondition(predicates.size() > MAX_PREDICATES, "At most " + MAX_PREDICATES + " predicates are allowed");
        }

        @Override
        public StructCodec<AllOf> codec() {
            return CODEC;
        }
    }

    /**
     * Matches when the given predicate does not match.
     *
     * @param predicate the predicate to invert
     */
    record Not(PositionPredicate predicate) implements PositionPredicate {
        static final StructCodec<Not> CODEC = StructCodec.struct(
                "predicate", Codec.ForwardRef(() -> PositionPredicate.CODEC), Not::predicate,
                Not::new);

        @Override
        public StructCodec<Not> codec() {
            return CODEC;
        }
    }

    /**
     * Matches every position.
     */
    record True() implements PositionPredicate {
        static final StructCodec<True> CODEC = StructCodec.struct(True::new);

        @Override
        public StructCodec<True> codec() {
            return CODEC;
        }
    }

    /**
     * Matches a position no entity is standing in.
     *
     * @param offset the position to test, relative to the position being transformed
     */
    record Unobstructed(Point offset) implements PositionPredicate {
        static final StructCodec<Unobstructed> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(Vec.ZERO), Unobstructed::offset,
                Unobstructed::new);

        @Override
        public StructCodec<Unobstructed> codec() {
            return CODEC;
        }
    }

    /**
     * Matches a position within a height band.
     *
     * @param minInclusive the lowest height that matches
     * @param maxInclusive the highest height that matches
     */
    record HeightRange(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) implements PositionPredicate {
        static final StructCodec<HeightRange> CODEC = StructCodec.struct(
                "min_inclusive", VerticalAnchor.CODEC, HeightRange::minInclusive,
                "max_inclusive", VerticalAnchor.CODEC, HeightRange::maxInclusive,
                HeightRange::new);

        @Override
        public StructCodec<HeightRange> codec() {
            return CODEC;
        }
    }

    /**
     * Matches when every position of a box satisfies the given predicate.
     *
     * @param min   the lower corner of the box, at most 16 blocks on each axis and at most {@code max} on each axis
     * @param max   the upper corner of the box, at most 16 blocks on each axis
     * @param match the predicate every position of the box must satisfy
     */
    record VolumeMatch(Point min, Point max, PositionPredicate match) implements PositionPredicate {
        static final StructCodec<VolumeMatch> CODEC = StructCodec.struct(
                "min", Codec.BLOCK_POSITION, VolumeMatch::min,
                "max", Codec.BLOCK_POSITION, VolumeMatch::max,
                "match", Codec.ForwardRef(() -> PositionPredicate.CODEC), VolumeMatch::match,
                VolumeMatch::new);

        public VolumeMatch {
            checkOffset(min);
            checkOffset(max);
            Check.argCondition(min.blockX() > max.blockX() || min.blockY() > max.blockY() || min.blockZ() > max.blockZ(),
                    "Min must not exceed max on any axis");
        }

        @Override
        public StructCodec<VolumeMatch> codec() {
            return CODEC;
        }
    }
}
