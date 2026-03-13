package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public sealed interface WolfVariant extends WolfVariants permits WolfVariantImpl {
    //TODO(26.1) Direct codec
    Codec<WolfVariant> REGISTRY_CODEC = StructCodec.struct(
            "assets", Assets.CODEC, WolfVariant::assets,
            "baby_assets", Assets.CODEC, WolfVariant::babyAssets,
            WolfVariantImpl::new);

    NetworkBuffer.Type<RegistryKey<WolfVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::wolfVariant);
    Codec<RegistryKey<WolfVariant>> CODEC = RegistryKey.codec(Registries::wolfVariant);

    static WolfVariant create(Assets assets, Assets babyAssets) {
        return new WolfVariantImpl(assets, babyAssets);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new instance of the "minecraft:wolf_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<WolfVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("wolf_variant"), REGISTRY_CODEC, RegistryData.Resource.WOLF_VARIANTS);
    }

    Assets assets();

    Assets babyAssets();

    sealed interface Assets permits WolfVariantImpl.AssetsImpl {
        Codec<Assets> CODEC = StructCodec.struct(
                "wild", Codec.KEY, Assets::wild,
                "tame", Codec.KEY, Assets::tame,
                "angry", Codec.KEY, Assets::angry,
                Assets::create);

        static Builder builder() {
            return new Builder();
        }

        static Assets create(Key wild, Key tame, Key angry) {
            return new WolfVariantImpl.AssetsImpl(wild, tame, angry);
        }

        Key wild();

        Key tame();

        Key angry();

        final class Builder {
            private @UnknownNullability Key wild;
            private @UnknownNullability Key tame;
            private @UnknownNullability Key angry;

            private Builder() {
            }

            public Builder wild(Key wild) {
                this.wild = Objects.requireNonNull(wild, "wild");
                return this;
            }

            public Builder tame(Key tame) {
                this.tame = Objects.requireNonNull(tame, "tame");
                return this;
            }

            public Builder angry(Key angry) {
                this.angry = Objects.requireNonNull(angry, "angry");
                return this;
            }

            public Assets build() {
                return new WolfVariantImpl.AssetsImpl(wild, tame, angry);
            }
        }
    }

    final class Builder {
        private @UnknownNullability Assets assets;
        private @UnknownNullability Assets babyAssets;

        private Builder() {
        }

        public Builder assets(Assets assets) {
            this.assets = Objects.requireNonNull(assets, "assets");
            return this;
        }

        public Builder babyAssets(Assets babyAssets) {
            this.babyAssets = Objects.requireNonNull(babyAssets, "babyAssets");
            return this;
        }

        public WolfVariant build() {
            return new WolfVariantImpl(assets, babyAssets);
        }
    }
}
