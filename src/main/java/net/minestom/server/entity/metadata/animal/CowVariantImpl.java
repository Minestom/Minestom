package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

record CowVariantImpl(
        @NotNull CowVariant.Model model,
        @NotNull Key assetId
) implements CowVariant {

    public CowVariantImpl {
        // Builder may violate nullability constraints
        Check.notNull(model, "model");
        Check.notNull(assetId, "assetId");
    }
}
