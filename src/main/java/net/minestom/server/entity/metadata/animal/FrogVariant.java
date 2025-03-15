package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface FrogVariant extends FrogVariants permits FrogVariantImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<FrogVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::frogVariant, true);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<FrogVariant>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::frogVariant);

    BinaryTagSerializer<FrogVariant> REGISTRY_NBT_TYPE = BinaryTagTemplate.object(
            "asset_id", BinaryTagSerializer.KEY, FrogVariant::assetId,
            FrogVariantImpl::new);

    /**
     * Creates a new instance of the "minecraft:frog_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<FrogVariant> createDefaultRegistry() {
        return FrogVariants.createDefaultRegistry();
    }

    static @NotNull FrogVariant create(@NotNull Key assetId) {
        return new FrogVariantImpl(assetId);
    }

    @NotNull Key assetId();

}
