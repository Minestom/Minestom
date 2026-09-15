package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;


public sealed interface TrimMaterial extends Holder.Direct<TrimMaterial>, TrimMaterials permits TrimMaterialImpl {
    NetworkBuffer.Type<TrimMaterial> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, TrimMaterial::paletteId,
            NetworkBuffer.COMPONENT, TrimMaterial::description,
            TrimMaterial::create);
    Codec<TrimMaterial> REGISTRY_CODEC = StructCodec.struct(
            "palette_id", Codec.KEY, TrimMaterial::paletteId,
            "description", Codec.COMPONENT, TrimMaterial::description,
            TrimMaterial::create);

    NetworkBuffer.Type<Holder<TrimMaterial>> NETWORK_TYPE = Holder.networkType(Registries::trimMaterial, REGISTRY_NETWORK_TYPE);
    Codec<Holder<TrimMaterial>> CODEC = Holder.codec(Registries::trimMaterial, REGISTRY_CODEC);

    static TrimMaterial create(
            Key paletteId,
            Component description
    ) {
        return new TrimMaterialImpl(paletteId, description);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for trim materials, loading the vanilla trim materials.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<TrimMaterial> createDefaultRegistry() {
        return DynamicRegistry.create(BuiltinRegistries.TRIM_MATERIAL, REGISTRY_CODEC);
    }

    Key paletteId();

    Component description();

    final class Builder {
        private Key paletteId;
        private Component description;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder paletteId(Key paletteId) {
            this.paletteId = paletteId;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        @Contract(pure = true)
        public TrimMaterial build() {
            return new TrimMaterialImpl(paletteId, description);
        }
    }

}
