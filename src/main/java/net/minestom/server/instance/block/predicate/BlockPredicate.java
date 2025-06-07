package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
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
 * @param blocks              The block names/tags to match.
 * @param state               The block properties to match.
 * @param nbt                 The block nbt to match.
 * @param componentPredicates Predicates to match a block entity's stored data components
 */
public record BlockPredicate(
        @Nullable RegistryTag<Block> blocks,
        @Nullable PropertiesPredicate state,
        @Nullable CompoundBinaryTag nbt,
        @Nullable DataComponentPredicates componentPredicates
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
            NBT_COMPOUND.optional(), BlockPredicate::nbt,
            DataComponentPredicates.NETWORK_TYPE, BlockPredicate::componentPredicates,
            BlockPredicate::new
    );

    public static final Codec<BlockPredicate> CODEC = StructCodec.struct(
            "blocks", RegistryTag.codec(Registries::blocks).optional(), BlockPredicate::blocks,
            "state", PropertiesPredicate.CODEC.optional(), BlockPredicate::state,
            "nbt", Codec.NBT_COMPOUND.optional(), BlockPredicate::nbt,
            StructCodec.INLINE, DataComponentPredicates.CODEC, BlockPredicate::componentPredicates,
            BlockPredicate::new
    );

    public BlockPredicate(@NotNull RegistryTag<Block> blocks) {
        this(blocks, null, null, null);
    }

    public BlockPredicate(@NotNull Block... blocks) {
        this(RegistryTag.direct(blocks));
    }

    public BlockPredicate(@NotNull PropertiesPredicate state) {
        this(null, state, null, null);
    }

    public BlockPredicate(@NotNull CompoundBinaryTag nbt) {
        this(null, null, nbt, null);
    }

    public BlockPredicate(@NotNull DataComponentMap components) {
        this(null, null, null, new DataComponentPredicates(components, null));
    }

    public BlockPredicate(@NotNull Map<DataComponentPredicates.ComponentPredicateType, DataComponentPredicate> predicates) {
        this(null, null, null, new DataComponentPredicates(null, predicates));
    }

    public BlockPredicate(@NotNull DataComponentPredicates predicates) {
        this(null, null, null, predicates);
    }

    public BlockPredicate(@Nullable RegistryTag<Block> blocks, @Nullable PropertiesPredicate state, @Nullable CompoundBinaryTag nbt) {
        this(blocks, state, nbt, null);
    }

    public BlockPredicate(@Nullable RegistryTag<Block> blocks, @Nullable PropertiesPredicate state, @Nullable CompoundBinaryTag nbt, @Nullable DataComponentPredicates componentPredicates) {
        this.blocks = blocks;
        this.state = state;
        this.nbt = nbt;
        this.componentPredicates = Objects.requireNonNullElseGet(componentPredicates, () -> new DataComponentPredicates(null, null));
    }

    @Override
    public boolean test(@NotNull Block block) {
        if (blocks != null && !blocks.contains(block))
            return false;
        if (state != null && !state.test(block))
            return false;
        if (nbt != null && !Objects.equals(nbt, BlockUtils.extractClientNbt(block)))
            return false;
        if (componentPredicates == null)
            return true;

        CompoundBinaryTag componentsTag = block.nbt() != null ? block.nbt().getCompound("components") : CompoundBinaryTag.empty();
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
        Result<DataComponentMap> result = DataComponent.MAP_NBT_TYPE.decode(coder, componentsTag);
        switch (result) {
            case Result.Ok(DataComponentMap components) -> {
                return componentPredicates.test(components);
            }
            case Result.Error<DataComponentMap> error -> throw new IllegalStateException(error.message());
        }
    }
}
