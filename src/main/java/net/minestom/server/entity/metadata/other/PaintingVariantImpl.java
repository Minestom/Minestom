package net.minestom.server.entity.metadata.other;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record PaintingVariantImpl(
        int width,
        int height,
        @NotNull Key assetId,
        @Nullable Component title,
        @Nullable Component author
) implements PaintingVariant {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    PaintingVariantImpl {
        Check.argCondition(assetId == null, "missing asset id");
        Check.argCondition(width <= 0, "width must be positive");
        Check.argCondition(height <= 0, "height must be positive");
    }
}
