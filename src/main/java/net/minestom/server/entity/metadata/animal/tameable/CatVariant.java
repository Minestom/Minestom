package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface CatVariant extends CatVariants permits CatVariantImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<CatVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::catVariant, true);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<CatVariant>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::catVariant);

    BinaryTagSerializer<CatVariant> REGISTRY_NBT_TYPE = BinaryTagTemplate.object(
            "asset_id", BinaryTagSerializer.KEY, CatVariant::assetId,
            CatVariantImpl::new);

    /**
     * Creates a new instance of the "minecraft:cat_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<CatVariant> createDefaultRegistry() {
        return CatVariants.createDefaultRegistry();
    }

    static @NotNull CatVariant create(@NotNull Key assetId) {
        return new CatVariantImpl(assetId);
    }

    @NotNull Key assetId();

}
