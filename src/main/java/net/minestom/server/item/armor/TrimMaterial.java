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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public sealed interface TrimMaterial extends Holder.Direct<TrimMaterial>, TrimMaterials permits TrimMaterialImpl {
    @NotNull NetworkBuffer.Type<TrimMaterial> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, TrimMaterial::assetName,
            NetworkBuffer.STRING.mapValue(NetworkBuffer.STRING), TrimMaterial::overrideArmorMaterials,
            NetworkBuffer.COMPONENT, TrimMaterial::description,
            TrimMaterial::create);
    @NotNull Codec<TrimMaterial> REGISTRY_CODEC = StructCodec.struct(
            "asset_name", Codec.STRING, TrimMaterial::assetName,
            "override_armor_materials", Codec.STRING.mapValue(Codec.STRING).optional(Map.of()), TrimMaterial::overrideArmorMaterials,
            "description", Codec.COMPONENT, TrimMaterial::description,
            TrimMaterial::create);

    @NotNull NetworkBuffer.Type<Holder<TrimMaterial>> NETWORK_TYPE = Holder.networkType(Registries::trimMaterial, REGISTRY_NETWORK_TYPE);
    @NotNull Codec<Holder<TrimMaterial>> CODEC = Holder.codec(Registries::trimMaterial, REGISTRY_CODEC);

    static @NotNull TrimMaterial create(
            @NotNull String assetName,
            @NotNull Map<String, String> overrideArmorMaterials,
            @NotNull Component description
    ) {
        return new TrimMaterialImpl(assetName, overrideArmorMaterials, description);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for trim materials, loading the vanilla trim materials.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<TrimMaterial> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("trim_material"), REGISTRY_CODEC, RegistryData.Resource.TRIM_MATERIALS);
    }

    @NotNull String assetName();

    @NotNull Map<String, String> overrideArmorMaterials();

    @NotNull Component description();

    final class Builder {
        private String assetName;
        private Material ingredient;
        private final Map<String, String> overrideArmorMaterials = new HashMap<>();
        private Component description;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder assetName(@NotNull String assetName) {
            this.assetName = assetName;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder overrideArmorMaterials(@NotNull Map<String, String> overrideArmorMaterials) {
            this.overrideArmorMaterials.putAll(overrideArmorMaterials);
            return this;
        }

        @Contract(value = "_, _ -> this", pure = true)
        public @NotNull Builder overrideArmorMaterial(@NotNull String slot, @NotNull String material) {
            this.overrideArmorMaterials.put(slot, material);
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        @Contract(pure = true)
        public @NotNull TrimMaterial build() {
            return new TrimMaterialImpl(assetName, overrideArmorMaterials, description);
        }
    }

}
