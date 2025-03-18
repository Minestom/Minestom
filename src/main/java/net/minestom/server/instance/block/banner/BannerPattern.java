package net.minestom.server.instance.block.banner;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface BannerPattern extends ProtocolObject, BannerPatterns permits BannerPatternImpl {
    @NotNull NetworkBuffer.Type<BannerPattern> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, BannerPattern::assetId,
            NetworkBuffer.STRING, BannerPattern::translationKey,
            BannerPattern::create);
    @NotNull Codec<BannerPattern> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, BannerPattern::assetId,
            "translation_key", Codec.STRING, BannerPattern::translationKey,
            BannerPattern::create);

    @NotNull NetworkBuffer.Type<Holder<BannerPattern>> HOLDER_NETWORK_TYPE = Holder.networkType(Registries::bannerPattern, BannerPattern.REGISTRY_NETWORK_TYPE);
    @NotNull Codec<Holder<BannerPattern>> HOLDER_CODEC = Holder.codec(Registries::bannerPattern, BannerPattern.REGISTRY_CODEC);

    static @NotNull BannerPattern create(
            @NotNull Key assetId,
            @NotNull String translationKey
    ) {
        return new BannerPatternImpl(assetId, translationKey, null);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for banner patterns, loading the vanilla banner patterns.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<BannerPattern> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:banner_pattern", BannerPatternImpl.REGISTRY_NBT_TYPE, Registry.Resource.BANNER_PATTERNS,
                (namespace, props) -> new BannerPatternImpl(Registry.bannerPattern(namespace, props))
        );
    }

    @NotNull Key assetId();

    @NotNull String translationKey();

    @Nullable Registry.BannerPatternEntry registry();

    final class Builder {
        private Key assetId;
        private String translationKey;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder assetId(@NotNull Key assetId) {
            this.assetId = assetId;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder translationKey(@NotNull String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        @Contract(pure = true)
        public @NotNull BannerPattern build() {
            return new BannerPatternImpl(assetId, translationKey, null);
        }
    }
}
