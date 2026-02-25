package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;

public record FrogVariantImpl(
        Key assetId
) implements FrogVariant {

    public FrogVariantImpl {
        // Builder may violate nullability constraints
        Check.notNull(assetId, "asset_id");
    }
}
