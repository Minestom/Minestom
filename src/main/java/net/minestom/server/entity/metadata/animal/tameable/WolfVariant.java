package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public sealed interface WolfVariant extends WolfVariants permits WolfVariantImpl {
    Codec<WolfVariant> REGISTRY_CODEC = StructCodec.struct(
            "assets", Assets.CODEC, WolfVariant::assets,
            WolfVariantImpl::new);

    NetworkBuffer.Type<RegistryKey<WolfVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::wolfVariant);
    Codec<RegistryKey<WolfVariant>> CODEC = RegistryKey.codec(Registries::wolfVariant);

    static WolfVariant create(Assets assets) {
        return new WolfVariantImpl(assets);
    }

    static WolfVariant create(Key wild, Key tame, Key angry) {
        return new WolfVariantImpl(new Assets(wild, tame, angry));
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
        return DynamicRegistry.create(Key.key("minecraft:wolf_variant"), REGISTRY_CODEC, RegistryData.Resource.WOLF_VARIANTS);
    }

    Assets assets();

    record Assets(Key wild, Key tame, Key angry) {
        public static final Codec<Assets> CODEC = StructCodec.struct(
                "wild", Codec.KEY, Assets::wild,
                "tame", Codec.KEY, Assets::tame,
                "angry", Codec.KEY, Assets::angry,
                Assets::new);

        public Assets {
            // Builder may violate nullability constraints
            Check.notNull(wild, "missing wild asset");
            Check.notNull(tame, "missing tame asset");
            Check.notNull(angry, "missing angry asset");
        }
    }

    final class Builder {
        private Assets assets;
        private Key wildAsset;
        private Key tameAsset;
        private Key angryAsset;

        private Builder() {
        }

        public Builder wildAsset(Key wildAsset) {
            this.wildAsset = wildAsset;
            return this;
        }

        public Builder tameAsset(Key tameAsset) {
            this.tameAsset = tameAsset;
            return this;
        }

        public Builder angryAsset(Key angryAsset) {
            this.angryAsset = angryAsset;
            return this;
        }

        public Builder assets(Assets assets) {
            this.assets = assets;
            return this;
        }

        public WolfVariant build() {
            final Assets assets = Objects.requireNonNullElseGet(this.assets, () -> new Assets(wildAsset, tameAsset, angryAsset));
            return new WolfVariantImpl(assets);
        }
    }
}
