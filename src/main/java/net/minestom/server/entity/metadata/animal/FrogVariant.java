package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;

public sealed interface FrogVariant extends FrogVariants permits FrogVariantImpl {
    Codec<FrogVariant> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, FrogVariant::assetId,
            FrogVariantImpl::new);

    NetworkBuffer.Type<RegistryKey<FrogVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::frogVariant);
    Codec<RegistryKey<FrogVariant>> CODEC = RegistryKey.codec(Registries::frogVariant);

    /**
     * Creates a new instance of the "minecraft:frog_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<FrogVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:frog_variant"), REGISTRY_CODEC, RegistryData.Resource.FROG_VARIANTS);
    }

    static FrogVariant create(Key assetId) {
        return new FrogVariantImpl(assetId);
    }

    Key assetId();

}
