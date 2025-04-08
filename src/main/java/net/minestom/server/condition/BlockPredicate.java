package net.minestom.server.condition;

import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Direction;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BlockPredicate {

    @NotNull StructCodec<BlockPredicate> CODEC = Codec.RegistryTaggedUnion(
            Registries::blockPredicate, BlockPredicate::codec, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<StructCodec<? extends BlockPredicate>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends BlockPredicate>> registry = DynamicRegistry.create("minestom:block_state_provider");
        registry.register("true", True.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("all_of", AllOf.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("any_of", AnyOf.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("has_sturdy_face", HasSturdyFace.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("inside_world_bounds", InsideWorldBounds.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("matching_block_tag", MatchingBlockTag.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("matching_blocks", MatchingBlocks.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("matching_fluids", MatchingFluids.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("not", Not.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("replaceable", Replaceable.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("solid", Solid.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("would_survive", WouldSurvive.CODEC, DataPack.MINECRAFT_CORE);
        registry.register("unobstructed", Unobstructed.CODEC, DataPack.MINECRAFT_CORE);
        return registry;
    }

    boolean test(Instance instance, Point blockPosition);

    @NotNull StructCodec<? extends BlockPredicate> codec();

    record True() implements BlockPredicate {
        public static final True INSTANCE = new True();
        public static final StructCodec<True> CODEC = StructCodec.struct(() -> INSTANCE);

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            return true;
        }

        @Override
        public @NotNull StructCodec<True> codec() {
            return CODEC;
        }
    }

    record AllOf(@NotNull List<BlockPredicate> predicates) implements BlockPredicate {
        public static final StructCodec<AllOf> CODEC = StructCodec.struct(
                "predicates", BlockPredicate.CODEC.list(), AllOf::predicates,
                AllOf::new
        );

        public AllOf {
            predicates = List.copyOf(predicates);
        }

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            for (BlockPredicate predicate : predicates) {
                if (!predicate.test(instance, blockPosition))
                    return false;
            }

            return true;
        }

        @Override
        public @NotNull StructCodec<AllOf> codec() {
            return CODEC;
        }
    }

    record AnyOf(@NotNull List<BlockPredicate> predicates) implements BlockPredicate {
        public static final StructCodec<AnyOf> CODEC = StructCodec.struct(
                "predicates", BlockPredicate.CODEC.list(), AnyOf::predicates,
                AnyOf::new
        );

        public AnyOf {
            predicates = List.copyOf(predicates);
        }

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            for (BlockPredicate predicate : predicates) {
                if (predicate.test(instance, blockPosition))
                    return true;
            }

            return false;
        }

        @Override
        public @NotNull StructCodec<AnyOf> codec() {
            return CODEC;
        }
    }

    record HasSturdyFace(
            @Nullable Point offset,
            @NotNull BlockFace direction
    ) implements BlockPredicate {
        public static final StructCodec<HasSturdyFace> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), HasSturdyFace::offset,
                "direction", Codec.Enum(Direction.class)
                        .transform(BlockFace::fromDirection, BlockFace::toDirection), HasSturdyFace::direction,
                HasSturdyFace::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return block.registry().collisionShape().isFaceFull(direction);
        }

        @Override
        public @NotNull StructCodec<HasSturdyFace> codec() {
            return CODEC;
        }
    }

    record InsideWorldBounds(@Nullable Point offset) implements BlockPredicate {
        public static final StructCodec<InsideWorldBounds> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), InsideWorldBounds::offset,
                InsideWorldBounds::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            int y = blockPosition.blockY();
            DimensionType dimensionType = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
            assert dimensionType != null;
            return y >= dimensionType.minY() && y < dimensionType.maxY();
        }

        @Override
        public @NotNull StructCodec<InsideWorldBounds> codec() {
            return CODEC;
        }
    }

    record MatchingBlockTag(
            @Nullable Point offset,
            @NotNull String tag
    ) implements BlockPredicate {
        public static final StructCodec<MatchingBlockTag> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), MatchingBlockTag::offset,
                "tag", Codec.STRING, MatchingBlockTag::tag,
                MatchingBlockTag::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Tag registeredTag = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, tag);
            if (registeredTag == null) return false;

            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return registeredTag.contains(block.key());
        }

        @Override
        public @NotNull StructCodec<MatchingBlockTag> codec() {
            return CODEC;
        }
    }

    record MatchingBlocks(
            @Nullable Point offset,
            @NotNull ObjectSet blocks
    ) implements BlockPredicate {
        public static final StructCodec<MatchingBlocks> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), MatchingBlocks::offset,
                "blocks", ObjectSet.codec(Tag.BasicType.BLOCKS), MatchingBlocks::blocks,
                MatchingBlocks::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return blocks.contains(block);
        }

        @Override
        public @NotNull StructCodec<MatchingBlocks> codec() {
            return CODEC;
        }
    }

    record MatchingFluids(
            @Nullable Point offset,
            @NotNull ObjectSet fluids
    ) implements BlockPredicate {
        public static final StructCodec<MatchingFluids> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), MatchingFluids::offset,
                "fluids", ObjectSet.codec(Tag.BasicType.FLUIDS), MatchingFluids::fluids,
                MatchingFluids::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            throw new UnsupportedOperationException("MatchingFluids is not implemented");
        }

        @Override
        public @NotNull StructCodec<MatchingFluids> codec() {
            return CODEC;
        }
    }

    record Not(@NotNull BlockPredicate predicate) implements BlockPredicate {
        public static final StructCodec<Not> CODEC = StructCodec.struct(
                "predicate", BlockPredicate.CODEC, Not::predicate,
                Not::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            return !predicate.test(instance, blockPosition);
        }

        @Override
        public @NotNull StructCodec<Not> codec() {
            return CODEC;
        }
    }

    record Replaceable(@Nullable Point offset) implements BlockPredicate {
        public static final StructCodec<Replaceable> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), Replaceable::offset,
                Replaceable::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return block.registry().isReplaceable();
        }

        @Override
        public @NotNull StructCodec<Replaceable> codec() {
            return CODEC;
        }
    }

    record Solid(@Nullable Point offset) implements BlockPredicate {
        public static final StructCodec<Solid> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), Solid::offset,
                Solid::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return block.registry().isSolid();
        }

        @Override
        public @NotNull StructCodec<Solid> codec() {
            return CODEC;
        }
    }

    record WouldSurvive(
            @Nullable Point offset,
            @NotNull Block state
    ) implements BlockPredicate {
        public static final StructCodec<WouldSurvive> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), WouldSurvive::offset,
                "state", Block.CODEC, WouldSurvive::state,
                WouldSurvive::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            throw new UnsupportedOperationException("WouldSurvive is not implemented");
        }

        @Override
        public @NotNull StructCodec<WouldSurvive> codec() {
            return CODEC;
        }
    }

    record Unobstructed(@Nullable Point offset) implements BlockPredicate {
        public static final StructCodec<Unobstructed> CODEC = StructCodec.struct(
                "offset", Codec.BLOCK_POSITION.optional(), Unobstructed::offset,
                Unobstructed::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Point position = blockPosition.add(offset == null ? Vec.ZERO : offset);
            Shape fullBlock = Block.STONE.registry().collisionShape();
            for (Entity entity : instance.getNearbyEntities(blockPosition, 3)) {
                if (fullBlock.intersectBox(entity.getPosition().sub(position), entity.getBoundingBox()))
                    return false;
            }

            return true;
        }

        @Override
        public @NotNull StructCodec<Unobstructed> codec() {
            return CODEC;
        }
    }
}
