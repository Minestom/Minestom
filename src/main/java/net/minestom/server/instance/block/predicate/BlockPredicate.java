package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static net.minestom.server.network.NetworkBuffer.NBT_COMPOUND;

/**
 * <p>A predicate to filter blocks based on their name, properties, and/or nbt.</p>
 *
 * <p>Note: Inline with vanilla, providing none of the filters will match any block.</p>
 *
 * <p>Note: To match the vanilla behavior of comparing block NBT, the NBT predicate
 * will ONLY match data which would be sent to the client eg with
 * {@link BlockHandler#getBlockEntityTags()}. This is relevant because this structure
 * is used for matching adventure mode blocks and must line up with client prediction.</p>
 *
 * @param blocks The block names/tags to match.
 * @param state  The block properties to match.
 * @param nbt    The block nbt to match.
 */
public record BlockPredicate(
        @Nullable BlockTypeFilter blocks,
        @Nullable PropertiesPredicate state,
        @Nullable CompoundBinaryTag nbt
) implements Predicate<Block> {
    /**
     * Matches all blocks.
     */
    public static final BlockPredicate ALL = new BlockPredicate(null, null, null);
    /**
     * <p>Matches no blocks.</p>
     *
     * <p>Works based on the property that an exact property will never match a property which doesnt exist on any block.</p>
     */
    public static final BlockPredicate NONE = new BlockPredicate(null, new PropertiesPredicate(Map.of("no_such_property", new PropertiesPredicate.ValuePredicate.Exact("never"))), null);

    public static final NetworkBuffer.Type<BlockPredicate> NETWORK_TYPE = NetworkBufferTemplate.template(
            BlockTypeFilter.NETWORK_TYPE.optional(), BlockPredicate::blocks,
            PropertiesPredicate.NETWORK_TYPE.optional(), BlockPredicate::state,
            NBT_COMPOUND.optional(), BlockPredicate::nbt,
            BlockPredicate::new
    );

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

    public BlockPredicate(@NotNull Block... blocks) {
        this(new BlockTypeFilter.Blocks(blocks));
    }

    public BlockPredicate(@NotNull PropertiesPredicate state) {
        this(null, state, null);
    }

    public BlockPredicate(@NotNull CompoundBinaryTag nbt) {
        this(null, null, nbt);
    }

    @Override
    public boolean test(@NotNull Block block) {
        if (blocks != null && !blocks.test(block))
            return false;
        if (state != null && !state.test(block))
            return false;
        return nbt == null || Objects.equals(nbt, BlockUtils.extractClientNbt(block));
    }
}
