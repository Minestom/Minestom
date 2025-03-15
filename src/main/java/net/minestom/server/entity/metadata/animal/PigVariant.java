package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface PigVariant extends PigVariants permits PigVariantImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<PigVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::pigVariant, true);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<PigVariant>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::pigVariant);

    BinaryTagSerializer<PigVariant> REGISTRY_NBT_TYPE = BinaryTagTemplate.object(
            "model", Model.NBT_TYPE.optional(Model.NORMAL), PigVariant::model,
            "asset_id", BinaryTagSerializer.KEY, PigVariant::assetId,
            PigVariantImpl::new);

    /**
     * Creates a new instance of the "minecraft:wolf_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<PigVariant> createDefaultRegistry() {
        return PigVariants.createDefaultRegistry();
    }

    static @NotNull PigVariant create(@NotNull Model model, @NotNull Key assetId) {
        return new PigVariantImpl(model, assetId);
    }

    @NotNull Model model();

    @NotNull Key assetId();

    enum Model {
        NORMAL,
        COLD;

        public static final BinaryTagSerializer<Model> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Model.class);
    }
}
