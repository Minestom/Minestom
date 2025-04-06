package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface CatVariant extends CatVariants permits CatVariantImpl {
    Codec<CatVariant> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, CatVariant::assetId,
            CatVariantImpl::new);

    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<CatVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::catVariant, false);
    @NotNull Codec<DynamicRegistry.Key<CatVariant>> NBT_TYPE = Codec.RegistryKey(Registries::catVariant);

    static @NotNull CatVariant create(@NotNull Key assetId) {
        return new CatVariantImpl(assetId);
    }

    /**
     * Creates a new instance of the "minecraft:cat_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<CatVariant> createDefaultRegistry() {
        return DynamicRegistry.create("minecraft:cat_variant", REGISTRY_CODEC, RegistryData.Resource.CAT_VARIANTS);
    }

    @NotNull Key assetId();

}
