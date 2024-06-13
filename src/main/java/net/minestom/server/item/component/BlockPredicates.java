package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public record BlockPredicates(@NotNull List<BlockPredicate> predicates, boolean showInTooltip) implements Predicate<Block> {
    /**
     * Will never match any block.
     */
    public static final BlockPredicates NEVER = new BlockPredicates(List.of(), false);

    public static final NetworkBuffer.Type<BlockPredicates> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        private static final NetworkBuffer.Type<List<BlockPredicate>> PREDICATE_LIST_TYPE = BlockPredicate.NETWORK_TYPE.list(Short.MAX_VALUE);

        @Override
        public void write(@NotNull NetworkBuffer buffer, BlockPredicates value) {
            buffer.write(PREDICATE_LIST_TYPE, value.predicates);
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip);
        }

        @Override
        public BlockPredicates read(@NotNull NetworkBuffer buffer) {
            return new BlockPredicates(buffer.read(PREDICATE_LIST_TYPE), buffer.read(NetworkBuffer.BOOLEAN));
        }
    };
    public static final BinaryTagSerializer<BlockPredicates> NBT_TYPE = new BinaryTagSerializer<>() {
        private static final BinaryTagSerializer<List<BlockPredicate>> PREDICATES_LIST_TYPE = BlockPredicate.NBT_TYPE.list();

        @Override
        public @NotNull BinaryTag write(@NotNull BlockPredicates value) {
            return CompoundBinaryTag.builder()
                    .put("predicates", PREDICATES_LIST_TYPE.write(value.predicates))
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build();
        }

        @Override
        public @NotNull BlockPredicates read(@NotNull BinaryTag tag) {
            if (!(tag instanceof CompoundBinaryTag compound)) return BlockPredicates.NEVER;

            List<BlockPredicate> predicates;
            BinaryTag predicatesTag = compound.get("predicates");
            if (predicatesTag != null) {
                predicates = PREDICATES_LIST_TYPE.read(predicatesTag);
            } else {
                // Try to read as a single predicate
                predicates = List.of(BlockPredicate.NBT_TYPE.read(tag));
            }

            // This default is fine in either case because the single predicate shouldnt have this key anyway.
            // https://github.com/KyoriPowered/adventure/issues/1068
            boolean showInTooltip = compound.getBoolean("show_in_tooltip", true);

            return new BlockPredicates(predicates, showInTooltip);
        }
    };

    public BlockPredicates {
        predicates = List.copyOf(predicates);
    }

    public BlockPredicates(@NotNull List<BlockPredicate> predicates) {
        this(predicates, true);
    }

    public BlockPredicates(@NotNull BlockPredicate predicate) {
        this(List.of(predicate), true);
    }

    public BlockPredicates(@NotNull BlockPredicate predicate, boolean showInTooltip) {
        this(List.of(predicate), showInTooltip);
    }

    public @NotNull BlockPredicates withTooltip(boolean showInTooltip) {
        return new BlockPredicates(predicates, showInTooltip);
    }

    @Override
    public boolean test(Block block) {
        for (BlockPredicate predicate : predicates) {
            if (predicate.test(block)) {
                return true;
            }
        }
        return false;
    }
}
