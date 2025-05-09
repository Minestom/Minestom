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

public sealed interface CowVariant extends CowVariants permits CowVariantImpl {
    @NotNull Codec<CowVariant> REGISTRY_CODEC = StructCodec.struct(
            "model", Model.CODEC.optional(Model.NORMAL), CowVariant::model,
            "asset_id", Codec.KEY, CowVariant::assetId,
            CowVariantImpl::new);

    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<CowVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::cowVariant, false);
    @NotNull Codec<DynamicRegistry.Key<CowVariant>> CODEC = Codec.RegistryKey(Registries::cowVariant);

    static @NotNull CowVariant create(@NotNull Model model, @NotNull Key assetId) {
        return new CowVariantImpl(model, assetId);
    }

    /**
     * Creates a new instance of the "minecraft:cow_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<CowVariant> createDefaultRegistry() {
        return DynamicRegistry.create("minecraft:cow_variant", REGISTRY_CODEC, Registry.Resource.COW_VARIANTS);
    }

    @NotNull Model model();

    @NotNull Key assetId();

    enum Model {
        NORMAL,
        COLD,
        WARM;

        public static final Codec<Model> CODEC = Codec.Enum(Model.class);
    }
}
