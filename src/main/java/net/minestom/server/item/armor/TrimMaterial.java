package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;

public sealed interface TrimMaterial extends Holder.Direct<TrimMaterial>, TrimMaterials permits TrimMaterialImpl {
    NetworkBuffer.Type<TrimMaterial> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, TrimMaterial::assetName,
            NetworkBuffer.STRING.mapValue(NetworkBuffer.STRING), TrimMaterial::overrideArmorMaterials,
            NetworkBuffer.COMPONENT, TrimMaterial::description,
            TrimMaterial::create);
    Codec<TrimMaterial> REGISTRY_CODEC = StructCodec.struct(
            "asset_name", Codec.STRING, TrimMaterial::assetName,
            "override_armor_materials", Codec.STRING.mapValue(Codec.STRING).optional(Map.of()), TrimMaterial::overrideArmorMaterials,
            "description", Codec.COMPONENT, TrimMaterial::description,
            TrimMaterial::create);

    NetworkBuffer.Type<Holder<TrimMaterial>> NETWORK_TYPE = Holder.networkType(Registries::trimMaterial, REGISTRY_NETWORK_TYPE);
    Codec<Holder<TrimMaterial>> CODEC = Holder.codec(Registries::trimMaterial, REGISTRY_CODEC);

    static TrimMaterial create(
            String assetName,
            Map<String, String> overrideArmorMaterials,
            Component description
    ) {
        return new TrimMaterialImpl(assetName, overrideArmorMaterials, description);
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
        return DynamicRegistry.create(Key.key("minecraft:trim_material"), REGISTRY_CODEC, RegistryData.Resource.TRIM_MATERIALS);
    }

    String assetName();

    Map<String, String> overrideArmorMaterials();

    Component description();

    final class Builder {
        private String assetName;
        private Material ingredient;
        private final Map<String, String> overrideArmorMaterials = new HashMap<>();
        private Component description;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder assetName(String assetName) {
            this.assetName = assetName;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder overrideArmorMaterials(Map<String, String> overrideArmorMaterials) {
            this.overrideArmorMaterials.putAll(overrideArmorMaterials);
            return this;
        }

        @Contract(value = "_, _ -> this", pure = true)
        public Builder overrideArmorMaterial(String slot, String material) {
            this.overrideArmorMaterials.put(slot, material);
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        @Contract(pure = true)
        public TrimMaterial build() {
            return new TrimMaterialImpl(assetName, overrideArmorMaterials, description);
        }
    }

}
