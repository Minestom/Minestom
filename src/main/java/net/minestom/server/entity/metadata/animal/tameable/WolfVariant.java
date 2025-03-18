package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public sealed interface WolfVariant extends WolfVariants permits WolfVariantImpl {
    Codec<WolfVariant> REGISTRY_CODEC = StructCodec.struct(
            "assets", Assets.CODEC, WolfVariant::assets,
            WolfVariantImpl::new);

    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<WolfVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::wolfVariant, false);
    @NotNull Codec<DynamicRegistry.Key<WolfVariant>> CODEC = Codec.RegistryKey(Registries::wolfVariant);

    /**
     * Creates a new instance of the "minecraft:wolf_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<WolfVariant> createDefaultRegistry() {
        return WolfVariants.createDefaultRegistry();
    }

    static @NotNull WolfVariant create(@NotNull Assets assets) {
        return new WolfVariantImpl(assets);
    }

    static @NotNull WolfVariant create(@NotNull Key wild, @NotNull Key tame, @NotNull Key angry) {
        return new WolfVariantImpl(new Assets(wild, tame, angry));
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    @NotNull Assets assets();

    record Assets(@NotNull Key wild, @NotNull Key tame, @NotNull Key angry) {
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

        public @NotNull Builder wildAsset(@NotNull Key wildAsset) {
            this.wildAsset = wildAsset;
            return this;
        }

        public @NotNull Builder tameAsset(@NotNull Key tameAsset) {
            this.tameAsset = tameAsset;
            return this;
        }

        public @NotNull Builder angryAsset(@NotNull Key angryAsset) {
            this.angryAsset = angryAsset;
            return this;
        }

        public @NotNull Builder assets(@NotNull Assets assets) {
            this.assets = assets;
            return this;
        }

        public @NotNull WolfVariant build() {
            final Assets assets = Objects.requireNonNullElseGet(this.assets, () -> new Assets(wildAsset, tameAsset, angryAsset));
            return new WolfVariantImpl(assets);
        }
    }
}
