package net.minestom.server.item.armor;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public sealed interface TrimMaterial extends ProtocolObject permits TrimMaterialImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<TrimMaterial>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::trimMaterial, true);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<TrimMaterial>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::trimMaterial);

    static @NotNull TrimMaterial create(
            @NotNull String assetName,
            @NotNull Material ingredient,
            float itemModelIndex,
            @NotNull Map<String, String> overrideArmorMaterials,
            @NotNull Component description
    ) {
        return new TrimMaterialImpl(
                assetName, ingredient, itemModelIndex,
                overrideArmorMaterials, description, null
        );
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
        return DynamicRegistry.create(
                "minecraft:trim_material", TrimMaterialImpl.REGISTRY_NBT_TYPE, Registry.Resource.TRIM_MATERIALS,
                (namespace, props) -> new TrimMaterialImpl(Registry.trimMaterial(namespace, props))
        );
    }

    @NotNull String assetName();

    @NotNull Material ingredient();

    float itemModelIndex();

    @NotNull Map<String, String> overrideArmorMaterials();

    @NotNull Component description();

    /**
     * Returns the raw registry entry of this trim, only if the trim is a vanilla trim. Otherwise, returns null.
     */
    @Contract(pure = true)
    @Nullable Registry.TrimMaterialEntry registry();

    final class Builder {
        private String assetName;
        private Material ingredient;
        private float itemModelIndex;
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
        public @NotNull Builder ingredient(@NotNull Material ingredient) {
            this.ingredient = ingredient;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder itemModelIndex(float itemModelIndex) {
            this.itemModelIndex = itemModelIndex;
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
            return new TrimMaterialImpl(
                    assetName, ingredient, itemModelIndex,
                    overrideArmorMaterials, description, null
            );
        }
    }

}
