package net.minestom.server.instance.block.banner;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record BannerPatternImpl(
        @NotNull Key assetId,
        @NotNull String translationKey,
        @Nullable Registry.BannerPatternEntry registry
) implements BannerPattern {

    static final BinaryTagSerializer<BannerPattern> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
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
        Check.argCondition(assetId == null, "missing asset id");
        Check.argCondition(translationKey == null || translationKey.isEmpty(), "missing translation key");
    }

    BannerPatternImpl(@NotNull Registry.BannerPatternEntry registry) {
        this(registry.assetId(), registry.translationKey(), registry);
    }

}
