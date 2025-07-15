package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.block.BlockUtils;
import org.jspecify.annotations.Nullable;

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
        @Nullable RegistryTag<Block> blocks,
        @Nullable PropertiesPredicate state,
        @Nullable CompoundBinaryTag nbt,
        DataComponentPredicates components
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
            RegistryTag.networkType(Registries::blocks).optional(), BlockPredicate::blocks,
            PropertiesPredicate.NETWORK_TYPE.optional(), BlockPredicate::state,
            NBT_COMPOUND.optional(), BlockPredicate::nbt,
            DataComponentPredicates.NETWORK_TYPE, BlockPredicate::components,
            BlockPredicate::new);
    public static final StructCodec<BlockPredicate> CODEC = StructCodec.struct(
            "blocks", RegistryTag.codec(Registries::blocks).optional(), BlockPredicate::blocks,
            "state", PropertiesPredicate.CODEC.optional(), BlockPredicate::state,
            "nbt", Codec.NBT_COMPOUND.optional(), BlockPredicate::nbt,
            StructCodec.INLINE, DataComponentPredicates.CODEC, BlockPredicate::components,
            BlockPredicate::new);

    public BlockPredicate(RegistryTag<Block> blocks) {
        this(blocks, null, null);
    }

    public BlockPredicate(Block... blocks) {
        this(RegistryTag.direct(blocks));
    }

    public BlockPredicate(PropertiesPredicate state) {
        this(null, state, null);
    }

    public BlockPredicate(CompoundBinaryTag nbt) {
        this(null, null, nbt);
    }

    public BlockPredicate(@Nullable RegistryTag<Block> blocks, @Nullable PropertiesPredicate state, @Nullable CompoundBinaryTag nbt) {
        this(blocks, state, nbt, DataComponentPredicates.EMPTY);
    }

    @Override
    public boolean test(Block block) {
        if (blocks != null && !blocks.contains(block))
            return false;
        if (state != null && !state.test(block))
            return false;
        return nbt == null || Objects.equals(nbt, BlockUtils.extractClientNbt(block));
    }
}
