package net.minestom.server.config;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BlockPredicate {

    @NotNull BinaryTagSerializer<BlockPredicate> NBT_TYPE = BinaryTagSerializer.registryTaggedUnion(
            Registries::blockPredicates, BlockPredicate::nbtType, "type");

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BinaryTagSerializer<? extends BlockPredicate>> createDefaultRegistry() {
        final DynamicRegistry<BinaryTagSerializer<? extends BlockPredicate>> registry = DynamicRegistry.create("minestom:block_state_provider");
        registry.register("true", True.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("all_of", AllOf.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("any_of", AnyOf.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("has_sturdy_face", HasSturdyFace.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("inside_world_bounds", InsideWorldBounds.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("matching_block_tag", MatchingBlockTag.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("matching_blocks", MatchingBlocks.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("matching_fluids", MatchingFluids.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("not", Not.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("replaceable", Replaceable.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("solid", Solid.NBT_TYPE, DataPack.MINECRAFT_CORE);
        registry.register("would_survive", WouldSurvive.NBT_TYPE, DataPack.MINECRAFT_CORE);
        return registry;
    }

    boolean test(Instance instance, Point blockPosition);

    @NotNull BinaryTagSerializer<? extends BlockPredicate> nbtType();

    record True() implements BlockPredicate {
        public static final BinaryTagSerializer<True> NBT_TYPE = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull True value) {
                return CompoundBinaryTag.empty();
            }

            @Override
            public @NotNull True read(@NotNull Context context, @NotNull BinaryTag tag) {
                return INSTANCE;
            }
        };

        public static final True INSTANCE = new True();

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            return true;
        }

        @Override
        public @NotNull BinaryTagSerializer<True> nbtType() {
            return NBT_TYPE;
        }
    }

    record AllOf(@NotNull List<BlockPredicate> predicates) implements BlockPredicate {
        public static final BinaryTagSerializer<AllOf> NBT_TYPE = BinaryTagSerializer.object(
                "predicates", BlockPredicate.NBT_TYPE.list(), AllOf::predicates,
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
        public @NotNull BinaryTagSerializer<AllOf> nbtType() {
            return NBT_TYPE;
        }
    }

    record AnyOf(@NotNull List<BlockPredicate> predicates) implements BlockPredicate {
        public static final BinaryTagSerializer<AnyOf> NBT_TYPE = BinaryTagSerializer.object(
                "predicates", BlockPredicate.NBT_TYPE.list(), AnyOf::predicates,
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
        public @NotNull BinaryTagSerializer<AnyOf> nbtType() {
            return NBT_TYPE;
        }
    }

    record HasSturdyFace(
            @Nullable Point offset,
            @NotNull BlockFace direction
    ) implements BlockPredicate {
        public static final BinaryTagSerializer<HasSturdyFace> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, HasSturdyFace::offset,
                "direction", BinaryTagSerializer.fromEnumStringable(Direction.class)
                        .map(BlockFace::fromDirection, BlockFace::toDirection), HasSturdyFace::direction,
                HasSturdyFace::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return block.registry().collisionShape().isFaceFull(direction);
        }

        @Override
        public @NotNull BinaryTagSerializer<HasSturdyFace> nbtType() {
            return NBT_TYPE;
        }
    }

    record InsideWorldBounds(@Nullable Point offset) implements BlockPredicate {
        public static final BinaryTagSerializer<InsideWorldBounds> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, InsideWorldBounds::offset,
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
        public @NotNull BinaryTagSerializer<InsideWorldBounds> nbtType() {
            return NBT_TYPE;
        }
    }

    record MatchingBlockTag(
            @Nullable Point offset,
            @NotNull String tag
    ) implements BlockPredicate {
        public static final BinaryTagSerializer<MatchingBlockTag> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, MatchingBlockTag::offset,
                "tag", BinaryTagSerializer.STRING, MatchingBlockTag::tag,
                MatchingBlockTag::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Tag registeredTag = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, tag);
            if (registeredTag == null) return false;

            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return registeredTag.contains(block.namespace());
        }

        @Override
        public @NotNull BinaryTagSerializer<MatchingBlockTag> nbtType() {
            return NBT_TYPE;
        }
    }

    record MatchingBlocks(
            @Nullable Point offset,
            @NotNull ObjectSet blocks
    ) implements BlockPredicate {
        public static final BinaryTagSerializer<MatchingBlocks> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, MatchingBlocks::offset,
                "blocks", ObjectSet.nbtType(Tag.BasicType.BLOCKS), MatchingBlocks::blocks,
                MatchingBlocks::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return blocks.contains(block);
        }

        @Override
        public @NotNull BinaryTagSerializer<MatchingBlocks> nbtType() {
            return NBT_TYPE;
        }
    }

    record MatchingFluids(
            @Nullable Point offset,
            @NotNull ObjectSet fluids
    ) implements BlockPredicate {
        public static final BinaryTagSerializer<MatchingFluids> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, MatchingFluids::offset,
                "fluids", ObjectSet.nbtType(Tag.BasicType.FLUIDS), MatchingFluids::fluids,
                MatchingFluids::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            throw new UnsupportedOperationException("MatchingFluids is not implemented");
        }

        @Override
        public @NotNull BinaryTagSerializer<MatchingFluids> nbtType() {
            return NBT_TYPE;
        }
    }

    record Not(@NotNull BlockPredicate predicate) implements BlockPredicate {
        public static final BinaryTagSerializer<Not> NBT_TYPE = BinaryTagSerializer.object(
                "predicate", BlockPredicate.NBT_TYPE, Not::predicate,
                Not::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            return !predicate.test(instance, blockPosition);
        }

        @Override
        public @NotNull BinaryTagSerializer<Not> nbtType() {
            return NBT_TYPE;
        }
    }

    record Replaceable(@Nullable Point offset) implements BlockPredicate {
        public static final BinaryTagSerializer<Replaceable> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, Replaceable::offset,
                Replaceable::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return block.registry().isReplaceable();
        }

        @Override
        public @NotNull BinaryTagSerializer<Replaceable> nbtType() {
            return NBT_TYPE;
        }
    }

    record Solid(@Nullable Point offset) implements BlockPredicate {
        public static final BinaryTagSerializer<Solid> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, Solid::offset,
                Solid::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            Block block = instance.getBlock(blockPosition.add(offset == null ? Vec.ZERO : offset));
            return block.registry().isSolid();
        }

        @Override
        public @NotNull BinaryTagSerializer<Solid> nbtType() {
            return NBT_TYPE;
        }
    }

    record WouldSurvive(
            @Nullable Point offset,
            @NotNull Block state
    ) implements BlockPredicate {
        public static final BinaryTagSerializer<WouldSurvive> NBT_TYPE = BinaryTagSerializer.object(
                "offset", BinaryTagSerializer.BLOCK_POSITION, WouldSurvive::offset,
                "state", Block.NBT_TYPE, WouldSurvive::state,
                WouldSurvive::new
        );

        @Override
        public boolean test(Instance instance, Point blockPosition) {
            throw new UnsupportedOperationException("WouldSurvive is not implemented");
        }

        @Override
        public @NotNull BinaryTagSerializer<WouldSurvive> nbtType() {
            return NBT_TYPE;
        }
    }
}
