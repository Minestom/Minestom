package net.minestom.server.instance.block.transformer;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.generation.BlockStateProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * How an item turns one block into another when used on it, such as a shovel making a dirt path.
 *
 * @param transforms the transforms in the order they are tried, must not be empty
 */
public record BlockTransformer(List<BlockTransformData> transforms) implements BlockTransformers {
    private static final int MAX_TRANSFORMS = 200;

    public static final Codec<BlockTransformer> REGISTRY_CODEC = BlockTransformData.CODEC.list(MAX_TRANSFORMS)
            .transform(BlockTransformer::new, BlockTransformer::transforms);

    public static final NetworkBuffer.Type<RegistryKey<BlockTransformer>> NETWORK_TYPE = RegistryKey.networkType(Registries::blockTransformer);
    public static final Codec<RegistryKey<BlockTransformer>> CODEC = RegistryKey.codec(Registries::blockTransformer);

    /**
     * <p>Creates a new registry for block transformers, loading the vanilla transformers.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    public static DynamicRegistry<BlockTransformer> createDefaultRegistry(Registries registries) {
        return DynamicRegistry.create(BuiltinRegistries.BLOCK_TRANSFORMER, REGISTRY_CODEC, registries);
    }

    public BlockTransformer {
        transforms = List.copyOf(transforms);
        Check.argCondition(transforms.isEmpty() || transforms.size() > MAX_TRANSFORMS,
                "A transformer needs between 1 and " + MAX_TRANSFORMS + " transforms");
    }

    /**
     * One transform an item may apply.
     *
     * @param blockStateProvider  the provider of the state the block is turned into, by key or inline
     * @param sound               the sound played on use
     * @param particle            the particle effect played on use
     * @param disallowedFaces     the faces the block may not be used on
     * @param loot                the key of the loot table dropped by the transform, null to drop nothing
     * @param dropStrategy        where the drops appear
     * @param updateFromNeighbors whether the placed block is updated by its neighbors
     * @param transformType       which block shape the transform applies to
     * @param consumeOnUse        whether the used item is consumed
     * @param itemDamagePerUse    how much durability the used item loses, must not be negative
     */
    public record BlockTransformData(
            Holder<BlockStateProvider> blockStateProvider,
            SoundEvent sound,
            TransformParticle particle,
            List<Direction> disallowedFaces,
            @Nullable Key loot,
            DropStrategy dropStrategy,
            boolean updateFromNeighbors,
            TransformType transformType,
            boolean consumeOnUse,
            int itemDamagePerUse
    ) {
        public static final Codec<BlockTransformData> CODEC = StructCodec.struct(
                "block_state_provider", BlockStateProvider.CODEC, BlockTransformData::blockStateProvider,
                "sound", SoundEvent.CODEC.optional(SoundEvent.INTENTIONALLY_EMPTY), BlockTransformData::sound,
                "particle", TransformParticle.CODEC.optional(TransformParticle.NONE), BlockTransformData::particle,
                "disallowed_faces", Direction.CODEC.list(Direction.values().length).optional(List.of()), BlockTransformData::disallowedFaces,
                "loot", Codec.KEY.optional(), BlockTransformData::loot,
                "drop_strategy", DropStrategy.CODEC.optional(DropStrategy.FROM_MIDDLE), BlockTransformData::dropStrategy,
                "update_from_neighbors", Codec.BOOLEAN.optional(true), BlockTransformData::updateFromNeighbors,
                "transform_type", TransformType.CODEC.optional(TransformType.SINGLE_BLOCK), BlockTransformData::transformType,
                "consume_on_use", Codec.BOOLEAN.optional(true), BlockTransformData::consumeOnUse,
                "item_damage_per_use", Codec.INT.optional(0), BlockTransformData::itemDamagePerUse,
                BlockTransformData::new);

        public BlockTransformData {
            disallowedFaces = List.copyOf(disallowedFaces);
            Check.argCondition(itemDamagePerUse < 0, "Item damage per use must not be negative");
        }
    }

    public enum TransformParticle {
        NONE,
        SCRAPE,
        WAX_ON,
        WAX_OFF;

        public static final Codec<TransformParticle> CODEC = Codec.Enum(TransformParticle.class);
    }

    public enum DropStrategy {
        CLICKED_FACE,
        FROM_MIDDLE;

        public static final Codec<DropStrategy> CODEC = Codec.Enum(DropStrategy.class);
    }

    public enum TransformType {
        SINGLE_BLOCK,
        COPPER_CHEST;

        public static final Codec<TransformType> CODEC = Codec.Enum(TransformType.class);
    }
}
