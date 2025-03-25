package net.minestom.server.entity.metadata.other;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface PaintingVariant extends PaintingVariants permits PaintingVariantImpl {
    @NotNull NetworkBuffer.Type<PaintingVariant> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, PaintingVariant::assetId,
            NetworkBuffer.INT, PaintingVariant::width,
            NetworkBuffer.INT, PaintingVariant::height,
            NetworkBuffer.COMPONENT.optional(), PaintingVariant::title,
            NetworkBuffer.COMPONENT.optional(), PaintingVariant::author,
            PaintingVariant::create);
    @NotNull Codec<PaintingVariant> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, PaintingVariant::assetId,
            "width", Codec.INT, PaintingVariant::width,
            "height", Codec.INT, PaintingVariant::height,
            "title", Codec.COMPONENT.optional(), PaintingVariant::title,
            "author", Codec.COMPONENT.optional(), PaintingVariant::author,
            PaintingVariant::create);

    @NotNull NetworkBuffer.Type<Holder<PaintingVariant>> NETWORK_TYPE = Holder.networkType(Registries::paintingVariant, REGISTRY_NETWORK_TYPE);
    @NotNull Codec<Holder<PaintingVariant>> CODEC = Holder.codec(Registries::paintingVariant, REGISTRY_CODEC);

    static @NotNull PaintingVariant create(
            @NotNull Key assetId,
            int width, int height,
            @Nullable Component title,
            @Nullable Component author
    ) {
        return new PaintingVariantImpl(assetId, width, height, title, author);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for painting variants, loading the vanilla painting variants.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<PaintingVariant> createDefaultRegistry() {
        return DynamicRegistry.create("minecraft:painting_variant", REGISTRY_CODEC, Registry.Resource.PAINTING_VARIANTS);
    }

    @NotNull Key assetId();

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
        public @NotNull Builder assetId(@NotNull Key assetId) {
            this.assetId = assetId;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder width(int width) {
            this.width = width;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder height(int height) {
            this.height = height;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder title(@Nullable Component title) {
            this.title = title;
            return this;
        }

        @Contract(value = "_ -> this", pure = true)
        public @NotNull Builder author(@Nullable Component author) {
            this.author = author;
            return this;
        }

        public @NotNull PaintingVariant build() {
            return new PaintingVariantImpl(assetId, width, height, title, author);
        }
    }
}
