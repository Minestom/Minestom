package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record PigVariantImpl(
        @NotNull PigVariant.Model model,
        @NotNull Key assetId
) implements PigVariant {

    public PigVariantImpl {
        // Builder may violate nullability constraints
        Check.notNull(model, "model");
        Check.notNull(assetId, "assetId");
    }
}
