package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface FrogVariant extends FrogVariants permits FrogVariantImpl {
    Codec<FrogVariant> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, FrogVariant::assetId,
            FrogVariantImpl::new);

    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<FrogVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::frogVariant, false);
    @NotNull Codec<DynamicRegistry.Key<FrogVariant>> CODEC = Codec.RegistryKey(Registries::frogVariant);

    /**
     * Creates a new instance of the "minecraft:frog_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<FrogVariant> createDefaultRegistry() {
        return DynamicRegistry.create("minecraft:frog_variant", REGISTRY_CODEC, Registry.Resource.FROG_VARIANTS);
    }

    static @NotNull FrogVariant create(@NotNull Key assetId) {
        return new FrogVariantImpl(assetId);
    }

    @NotNull Key assetId();

}
