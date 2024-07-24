package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
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

public sealed interface TrimPattern extends ProtocolObject permits TrimPatternImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<TrimPattern>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::trimPattern);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<TrimPattern>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::trimPattern);

    static @NotNull TrimPattern create(
            @NotNull Key assetId,
            @NotNull Material template,
            @NotNull Component description,
            boolean decal
    ) {
        return new TrimPatternImpl(assetId, template, description, decal, null);
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
    static @NotNull DynamicRegistry<TrimPattern> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:trim_pattern", TrimPatternImpl.REGISTRY_NBT_TYPE, Registry.Resource.TRIM_PATTERNS,
                (namespace, props) -> new TrimPatternImpl(Registry.trimPattern(namespace, props))
        );
    }

    @NotNull Key assetId();

    @NotNull Material template();

    @NotNull Component description();

    boolean isDecal();

    @Contract(pure = true)
    @Nullable Registry.TrimPatternEntry registry();

    final class Builder {
        private Key assetId;
        private Material template;
        private Component description;
        private boolean decal;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder assetId(@NotNull String assetId) {
            return assetId(Key.key(assetId));
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder assetId(@NotNull Key assetId) {
            this.assetId = assetId;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder template(@NotNull Material template) {
            this.template = template;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder decal(boolean decal) {
            this.decal = decal;
            return this;
        }

        @Contract(pure = true)
        public @NotNull TrimPattern build() {
            return new TrimPatternImpl(assetId, template, description, decal, null);
        }
    }
}
