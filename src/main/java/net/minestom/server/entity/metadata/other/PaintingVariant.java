package net.minestom.server.entity.metadata.other;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface PaintingVariant extends Holder.Direct<PaintingVariant>, PaintingVariants permits PaintingVariantImpl {
    NetworkBuffer.Type<PaintingVariant> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.INT, PaintingVariant::width,
            NetworkBuffer.INT, PaintingVariant::height,
            NetworkBuffer.KEY, PaintingVariant::assetId,
            NetworkBuffer.COMPONENT.optional(), PaintingVariant::title,
            NetworkBuffer.COMPONENT.optional(), PaintingVariant::author,
            PaintingVariantImpl::new);
    Codec<PaintingVariant> REGISTRY_CODEC = StructCodec.struct(
            "width", Codec.INT, PaintingVariant::width,
            "height", Codec.INT, PaintingVariant::height,
            "asset_id", Codec.KEY, PaintingVariant::assetId,
            "title", Codec.COMPONENT.optional(), PaintingVariant::title,
            "author", Codec.COMPONENT.optional(), PaintingVariant::author,
            PaintingVariantImpl::new);

    // For some unknown reason, the network type still uses a holder even though the codec does not.
    // This appears to be a mistake since stopping inline values was explicitly mentioned as a change in snapshot notes.
    // It would also not work on vanilla as serializing a painting entity with inline variant would fail.
    // However, we don't serialize painting entities, so we can allow this :) Use at your own risk.
    // IMPL: Please remove the workaround later if this is fixed.
    NetworkBuffer.Type<Holder<PaintingVariant>> NETWORK_TYPE = Holder.networkType(Registries::paintingVariant, REGISTRY_NETWORK_TYPE);
    Codec<Holder<PaintingVariant>> CODEC = RegistryKey.codec(Registries::paintingVariant)
            .transform(key -> key, Holder::asKey);

    static PaintingVariant create(
            Key assetId,
            int width, int height,
            @Nullable Component title,
            @Nullable Component author
    ) {
        return new PaintingVariantImpl(width, height, assetId, title, author);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for painting variants, loading the vanilla painting variants.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<PaintingVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:painting_variant"), REGISTRY_CODEC, RegistryData.Resource.PAINTING_VARIANTS);
    }

    Key assetId();

    int width();

    int height();

    @Nullable Component title();

    @Nullable Component author();

    class Builder {
        private Key assetId;
        private int width;
        private int height;
        private Component title;
        private Component author;

        private Builder() {
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder assetId(Key assetId) {
            this.assetId = assetId;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder width(int width) {
            this.width = width;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder title(@Nullable Component title) {
            this.title = title;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public Builder author(@Nullable Component author) {
            this.author = author;
            return this;
        }

        public PaintingVariant build() {
            return new PaintingVariantImpl(width, height, assetId, title, author);
        }
    }
}
