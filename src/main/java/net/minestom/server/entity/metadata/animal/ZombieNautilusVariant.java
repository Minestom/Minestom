package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.ApiStatus;

public sealed interface ZombieNautilusVariant extends ZombieNautilusVariants permits ZombieNautilusVariantImpl {
    Codec<ZombieNautilusVariant> REGISTRY_CODEC = StructCodec.struct(
            "model", Model.CODEC.optional(Model.NORMAL), ZombieNautilusVariant::model,
            "asset_id", Codec.KEY, ZombieNautilusVariant::assetId,
            ZombieNautilusVariantImpl::new);

    NetworkBuffer.Type<RegistryKey<ZombieNautilusVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::zombieNautilusVariant);
    Codec<RegistryKey<ZombieNautilusVariant>> CODEC = RegistryKey.codec(Registries::zombieNautilusVariant);

    static ZombieNautilusVariant create(Model model, Key assetId) {
        return new ZombieNautilusVariantImpl(model, assetId);
    }

    /**
     * Creates a new instance of the "minecraft:zombie_nautilus_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<ZombieNautilusVariant> createDefaultRegistry() {
        return DynamicRegistry.create(BuiltinRegistries.ZOMBIE_NAUTILUS_VARIANT, REGISTRY_CODEC, RegistryData.Resource.ZOMBIE_NAUTILUS_VARIANTS);
    }

    Model model();

    Key assetId();

    enum Model {
        NORMAL,
        WARM;

        public static final Codec<Model> CODEC = Codec.Enum(Model.class);
    }
}
