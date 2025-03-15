package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface CowVariant extends CowVariants permits CowVariantImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<CowVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::cowVariant, true);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<CowVariant>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::cowVariant);

    BinaryTagSerializer<CowVariant> REGISTRY_NBT_TYPE = BinaryTagTemplate.object(
            "model", Model.NBT_TYPE.optional(Model.NORMAL), CowVariant::model,
            "asset_id", BinaryTagSerializer.KEY, CowVariant::assetId,
            CowVariantImpl::new);

    /**
     * Creates a new instance of the "minecraft:wolf_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<CowVariant> createDefaultRegistry() {
        return CowVariants.createDefaultRegistry();
    }

    static @NotNull CowVariant create(@NotNull Model model, @NotNull Key assetId) {
        return new CowVariantImpl(model, assetId);
    }

    @NotNull Model model();

    @NotNull Key assetId();

    enum Model {
        NORMAL,
        COLD,
        WARM;

        public static final BinaryTagSerializer<Model> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Model.class);
    }
}
