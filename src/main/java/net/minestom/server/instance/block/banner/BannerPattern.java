package net.minestom.server.instance.block.banner;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

public sealed interface BannerPattern extends Holder.Direct<BannerPattern>, BannerPatterns permits BannerPatternImpl {
    NetworkBuffer.Type<BannerPattern> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, BannerPattern::assetId,
            NetworkBuffer.STRING, BannerPattern::translationKey,
            BannerPattern::create);
    Codec<BannerPattern> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, BannerPattern::assetId,
            "translation_key", Codec.STRING, BannerPattern::translationKey,
            BannerPattern::create);

    NetworkBuffer.Type<Holder<BannerPattern>> HOLDER_NETWORK_TYPE = Holder.networkType(Registries::bannerPattern, BannerPattern.REGISTRY_NETWORK_TYPE);
    Codec<Holder<BannerPattern>> HOLDER_CODEC = Holder.codec(Registries::bannerPattern, BannerPattern.REGISTRY_CODEC);

    static BannerPattern create(
            Key assetId,
            String translationKey
    ) {
        return new BannerPatternImpl(assetId, translationKey);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for banner patterns, loading the vanilla banner patterns.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<BannerPattern> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:banner_pattern"), REGISTRY_CODEC, RegistryData.Resource.BANNER_PATTERNS);
    }

    Key assetId();

    String translationKey();

    final class Builder {
        private Key assetId;
        private String translationKey;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder assetId(Key assetId) {
            this.assetId = assetId;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder translationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        @Contract(pure = true)
        public BannerPattern build() {
            return new BannerPatternImpl(assetId, translationKey);
        }
    }
}
