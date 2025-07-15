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

public sealed interface CowVariant extends CowVariants permits CowVariantImpl {
    Codec<CowVariant> REGISTRY_CODEC = StructCodec.struct(
            "model", Model.CODEC.optional(Model.NORMAL), CowVariant::model,
            "asset_id", Codec.KEY, CowVariant::assetId,
            CowVariantImpl::new);

    NetworkBuffer.Type<RegistryKey<CowVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::cowVariant);
    Codec<RegistryKey<CowVariant>> CODEC = RegistryKey.codec(Registries::cowVariant);

    static CowVariant create(Model model, Key assetId) {
        return new CowVariantImpl(model, assetId);
    }

    /**
     * Creates a new instance of the "minecraft:cow_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<CowVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:cow_variant"), REGISTRY_CODEC, RegistryData.Resource.COW_VARIANTS);
    }

    Model model();

    Key assetId();

    enum Model {
        NORMAL,
        COLD,
        WARM;

        public static final Codec<Model> CODEC = Codec.Enum(Model.class);
    }
}
