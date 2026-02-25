package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;

record CowVariantImpl(
        CowVariant.Model model,
        Key assetId
) implements CowVariant {

    public CowVariantImpl {
        // Builder may violate nullability constraints
        Check.notNull(model, "model");
        Check.notNull(assetId, "assetId");
    }
}
