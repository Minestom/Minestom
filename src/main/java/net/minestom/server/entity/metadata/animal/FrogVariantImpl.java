package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record FrogVariantImpl(
        @NotNull Key assetId
) implements FrogVariant {

    public FrogVariantImpl {
        // Builder may violate nullability constraints
        Check.notNull(assetId, "asset_id");
    }
}
