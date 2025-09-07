package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

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
 * @param blocks              The block names/tags to match.
 * @param state               The block properties to match.
 * @param nbt                 The block nbt to match.
 * @param components          The block data components to match.
 */
public record BlockPredicate(
        @Nullable RegistryTag<Block> blocks,
        @Nullable PropertiesPredicate state,
        @Nullable NbtPredicate nbt,
        DataComponentPredicates components
) implements Predicate<Block> {
    /**
     * Matches all blocks.
     */
    public static final BlockPredicate ALL = new BlockPredicate(null, null, null, null);
    /**
     * <p>Matches no blocks.</p>
     *
     * <p>Works based on the property that an exact property will never match a property which doesn't exist on any block.</p>
     */
    public static final BlockPredicate NONE = new BlockPredicate(null, new PropertiesPredicate(Map.of("no_such_property", new PropertiesPredicate.ValuePredicate.Exact("never"))), null, null);

    public static final NetworkBuffer.Type<BlockPredicate> NETWORK_TYPE = NetworkBufferTemplate.template(
            RegistryTag.networkType(Registries::blocks).optional(), BlockPredicate::blocks,
            PropertiesPredicate.NETWORK_TYPE.optional(), BlockPredicate::state,
            NbtPredicate.NETWORK_TYPE.optional(), BlockPredicate::nbt,
            DataComponentPredicates.NETWORK_TYPE, BlockPredicate::components,
            BlockPredicate::new
    );

    public static final Codec<BlockPredicate> CODEC = StructCodec.struct(
            "blocks", RegistryTag.codec(Registries::blocks).optional(), BlockPredicate::blocks,
            "state", PropertiesPredicate.CODEC.optional(), BlockPredicate::state,
            "nbt", NbtPredicate.CODEC.optional(), BlockPredicate::nbt,
            StructCodec.INLINE, DataComponentPredicates.CODEC, BlockPredicate::components,
            BlockPredicate::new
    );

    public BlockPredicate(RegistryTag<Block> blocks) {
        this(blocks, null, null, null);
    }

    public BlockPredicate(Block... blocks) {
        this(RegistryTag.direct(blocks));
    }

    public BlockPredicate(PropertiesPredicate state) {
        this(null, state, null, null);
    }

    public BlockPredicate(CompoundBinaryTag nbt) {
        this(new NbtPredicate(nbt));
    }

    public BlockPredicate(NbtPredicate nbt) {
        this(null, null, nbt, null);
    }

    public BlockPredicate(DataComponentMap components) {
        this(null, null, null, new DataComponentPredicates(components, null));
    }

    public BlockPredicate(ComponentPredicateSet predicates) {
        this(null, null, null, new DataComponentPredicates(null, predicates));
    }

    public BlockPredicate(DataComponentPredicates predicates) {
        this(null, null, null, predicates);
    }

    public BlockPredicate(@Nullable RegistryTag<Block> blocks, @Nullable PropertiesPredicate state, @Nullable NbtPredicate nbt) {
        this(blocks, state, nbt, null);
    }

    public BlockPredicate(@Nullable RegistryTag<Block> blocks, @Nullable PropertiesPredicate state, @Nullable NbtPredicate nbt, @Nullable DataComponentPredicates components) {
        this.blocks = blocks;
        this.state = state;
        this.nbt = nbt;
        this.components = Objects.requireNonNullElse(components, DataComponentPredicates.EMPTY);
    }

    @Override
    public boolean test(Block block) {
        if (blocks != null && !blocks.contains(block))
            return false;
        if (state != null && !state.test(block))
            return false;
        if (nbt != null && (block.nbt() == null || !nbt.test(BlockUtils.extractClientNbt(block))))
            return false;
        if ((components.exact() == null || components.exact().isEmpty()) && (components.predicates() == null || components.predicates().isEmpty()))
            return true;
        if (block.nbt() == null)
            return false; // If a block has no NBT (it's not a block entity), any component predicates must return false

        CompoundBinaryTag componentsTag = block.nbt().getCompound("components");
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
        DataComponentMap componentMap = DataComponent.MAP_NBT_TYPE.decode(coder, componentsTag).orElseThrow();
        return components.test(componentMap);
    }
}
