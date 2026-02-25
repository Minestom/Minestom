package net.minestom.server.instance.block.banner;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;

record BannerPatternImpl(
        Key assetId,
        String translationKey
) implements BannerPattern {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    BannerPatternImpl {
        Check.argCondition(assetId == null, "missing asset id");
        Check.argCondition(translationKey == null || translationKey.isEmpty(), "missing translation key");
    }

}
