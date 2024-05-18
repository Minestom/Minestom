package net.minestom.server.instance.block.banner;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record BannerPatternImpl(
        @NotNull NamespaceID namespace,
        @NotNull NamespaceID assetId,
        @NotNull String translationKey,
        @Nullable Registry.BannerPatternEntry registry
) implements BannerPattern {

    static final BinaryTagSerializer<BannerPattern> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("BannerPattern is read-only");
            },
            bannerPattern -> CompoundBinaryTag.builder()
                    .putString("asset_id", bannerPattern.assetId().asString())
                    .putString("translation_key", bannerPattern.translationKey())
                    .build()
    );

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    BannerPatternImpl {
        Check.notNull(namespace, "Namespace cannot be null");
        Check.argCondition(assetId == null, "missing asset id: {0}", namespace);
        Check.argCondition(translationKey == null || translationKey.isEmpty(), "missing translation key: {0}", namespace);
    }

    BannerPatternImpl(@NotNull Registry.BannerPatternEntry registry) {
        this(registry.namespace(), registry.assetId(), registry.translationKey(), registry);
    }

}
