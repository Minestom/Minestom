package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface TrimPattern extends ProtocolObject permits TrimPatternImpl {
    @NotNull NetworkBuffer.Type<TrimPattern> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, TrimPattern::assetId,
            NetworkBuffer.COMPONENT, TrimPattern::description,
            NetworkBuffer.BOOLEAN, TrimPattern::isDecal,
            TrimPattern::create);
    @NotNull Codec<TrimPattern> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, TrimPattern::assetId,
            "description", Codec.COMPONENT, TrimPattern::description,
            "decal", Codec.BOOLEAN, TrimPattern::isDecal,
            TrimPattern::create);

    @NotNull NetworkBuffer.Type<Holder<TrimPattern>> NETWORK_TYPE = Holder.networkType(Registries::trimPattern, REGISTRY_NETWORK_TYPE);
    @NotNull Codec<Holder<TrimPattern>> CODEC = Holder.codec(Registries::trimPattern, REGISTRY_CODEC);

    static @NotNull TrimPattern create(
            @NotNull Key assetId,
            @NotNull Component description,
            boolean decal
    ) {
        return new TrimPatternImpl(assetId, description, decal);
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
        return DynamicRegistry.create("minecraft:trim_pattern", REGISTRY_CODEC, Registry.Resource.TRIM_PATTERNS);
    }

    @NotNull Key assetId();

    @NotNull Component description();

    boolean isDecal();

    final class Builder {
        private Key assetId;
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
            return new TrimPatternImpl(assetId, description, decal);
        }
    }
}
