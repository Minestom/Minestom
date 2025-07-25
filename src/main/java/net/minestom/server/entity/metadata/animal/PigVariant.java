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
import org.jetbrains.annotations.NotNull;

public sealed interface PigVariant extends PigVariants permits PigVariantImpl {
    @NotNull Codec<PigVariant> REGISTRY_CODEC = StructCodec.struct(
            "model", Model.CODEC.optional(Model.NORMAL), PigVariant::model,
            "asset_id", Codec.KEY, PigVariant::assetId,
            PigVariantImpl::new);

    @NotNull NetworkBuffer.Type<RegistryKey<PigVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::pigVariant);
    @NotNull Codec<RegistryKey<PigVariant>> CODEC = RegistryKey.codec(Registries::pigVariant);

    /**
     * Creates a new instance of the "minecraft:pig_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<PigVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("pig_variant"), REGISTRY_CODEC, RegistryData.Resource.PIG_VARIANTS);
    }

    static @NotNull PigVariant create(@NotNull Model model, @NotNull Key assetId) {
        return new PigVariantImpl(model, assetId);
    }

    @NotNull Model model();

    @NotNull Key assetId();

    enum Model {
        NORMAL,
        COLD;

        public static final Codec<Model> CODEC = Codec.Enum(Model.class);
    }
}
