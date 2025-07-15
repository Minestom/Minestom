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

public sealed interface ChickenVariant extends ChickenVariants permits ChickenVariantImpl {
    Codec<ChickenVariant> REGISTRY_CODEC = StructCodec.struct(
            "model", Model.CODEC.optional(Model.NORMAL), ChickenVariant::model,
            "asset_id", Codec.KEY, ChickenVariant::assetId,
            ChickenVariantImpl::new);

    NetworkBuffer.Type<RegistryKey<ChickenVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::chickenVariant);
    Codec<RegistryKey<ChickenVariant>> CODEC = RegistryKey.codec(Registries::chickenVariant);

    static ChickenVariant create(Model model, Key assetId) {
        return new ChickenVariantImpl(model, assetId);
    }

    /**
     * Creates a new instance of the "minecraft:chicken_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<ChickenVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:chicken_variant"), REGISTRY_CODEC, RegistryData.Resource.CHICKEN_VARIANTS);
    }

    Model model();

    Key assetId();

    enum Model {
        NORMAL,
        COLD;

        public static final Codec<Model> CODEC = Codec.Enum(Model.class);
    }
}
