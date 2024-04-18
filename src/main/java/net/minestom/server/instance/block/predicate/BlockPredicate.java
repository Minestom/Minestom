package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record BlockPredicate(
        @Nullable BlockTypeFilter blocks,
        @Nullable PropertiesPredicate state,
        @Nullable CompoundBinaryTag nbt
) implements Predicate<Block> {
    /**
     * Matches all blocks.
     */
    public static final BlockPredicate ALL = new BlockPredicate(null, null, null);

    public static final NetworkBuffer.Type<BlockPredicate> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, BlockPredicate value) {
            buffer.writeOptional(BlockTypeFilter.NETWORK_TYPE, value.blocks);
            buffer.writeOptional(PropertiesPredicate.NETWORK_TYPE, value.state);
            buffer.writeOptional(NetworkBuffer.NBT, value.nbt);
        }

        @Override
        public BlockPredicate read(@NotNull NetworkBuffer buffer) {
            return new BlockPredicate(
                    buffer.readOptional(BlockTypeFilter.NETWORK_TYPE),
                    buffer.readOptional(PropertiesPredicate.NETWORK_TYPE),
                    (CompoundBinaryTag) buffer.readOptional(NetworkBuffer.NBT)
            );
        }
    };
    public static final BinaryTagSerializer<BlockPredicate> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull BlockPredicate value) {
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            if (value.blocks != null)
                builder.put("blocks", BlockTypeFilter.NBT_TYPE.write(value.blocks));
            if (value.state != null)
                builder.put("state", PropertiesPredicate.NBT_TYPE.write(value.state));
            if (value.nbt != null)
                builder.put("nbt", value.nbt);
            return builder.build();
        }

        @Override
        public @NotNull BlockPredicate read(@NotNull BinaryTag tag) {
            if (!(tag instanceof CompoundBinaryTag compound)) return BlockPredicate.ALL;

            BinaryTag entry;
            BlockTypeFilter blocks = null;
            if ((entry = compound.get("blocks")) != null)
                blocks = BlockTypeFilter.NBT_TYPE.read(entry);
            PropertiesPredicate state = null;
            if ((entry = compound.get("state")) != null)
                state = PropertiesPredicate.NBT_TYPE.read(entry);
            CompoundBinaryTag nbt = null;
            if ((entry = compound.get("nbt")) != null)
                nbt = BinaryTagSerializer.COMPOUND_COERCED.read(entry);
            return new BlockPredicate(blocks, state, nbt);
        }
    };

    public BlockPredicate(@NotNull BlockTypeFilter blocks) {
        this(blocks, null, null);
    }

    public BlockPredicate(@NotNull PropertiesPredicate state) {
        this(null, state, null);
    }

    public BlockPredicate(@NotNull CompoundBinaryTag nbt) {
        this(null, null, nbt);
    }

    @Override
    public boolean test(@NotNull Block block) {
        throw new UnsupportedOperationException("not implemented");
    }

}
