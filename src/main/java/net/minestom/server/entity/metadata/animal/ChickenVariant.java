package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface ChickenVariant extends ChickenVariants permits ChickenVariantImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<ChickenVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::chickenVariant, true);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<ChickenVariant>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::chickenVariant);

    BinaryTagSerializer<ChickenVariant> REGISTRY_NBT_TYPE = BinaryTagTemplate.object(
            "model", Model.NBT_TYPE.optional(Model.NORMAL), ChickenVariant::model,
            "asset_id", BinaryTagSerializer.KEY, ChickenVariant::assetId,
            ChickenVariantImpl::new);

    /**
     * Creates a new instance of the "minecraft:chicken_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<ChickenVariant> createDefaultRegistry() {
        return ChickenVariants.createDefaultRegistry();
    }

    static @NotNull ChickenVariant create(@NotNull Model model, @NotNull Key assetId) {
        return new ChickenVariantImpl(model, assetId);
    }

    @NotNull Model model();

    @NotNull Key assetId();

    enum Model {
        NORMAL,
        COLD;

        public static final BinaryTagSerializer<Model> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Model.class);
    }
}
