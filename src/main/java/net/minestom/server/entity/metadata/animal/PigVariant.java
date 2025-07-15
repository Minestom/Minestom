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

public sealed interface PigVariant extends PigVariants permits PigVariantImpl {
    Codec<PigVariant> REGISTRY_CODEC = StructCodec.struct(
            "model", Model.CODEC.optional(Model.NORMAL), PigVariant::model,
            "asset_id", Codec.KEY, PigVariant::assetId,
            PigVariantImpl::new);

    NetworkBuffer.Type<RegistryKey<PigVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::pigVariant);
    Codec<RegistryKey<PigVariant>> CODEC = RegistryKey.codec(Registries::pigVariant);

    /**
     * Creates a new instance of the "minecraft:pig_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<PigVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:pig_variant"), REGISTRY_CODEC, RegistryData.Resource.PIG_VARIANTS);
    }

    static PigVariant create(Model model, Key assetId) {
        return new PigVariantImpl(model, assetId);
    }

    Model model();

    Key assetId();

    enum Model {
        NORMAL,
        COLD;

        public static final Codec<Model> CODEC = Codec.Enum(Model.class);
    }
}
