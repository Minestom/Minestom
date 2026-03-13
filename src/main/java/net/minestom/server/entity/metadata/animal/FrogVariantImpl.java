package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;

import java.util.Objects;

public record FrogVariantImpl(
        Key assetId
) implements FrogVariant {

    public FrogVariantImpl {
        // Builder may violate nullability constraints
        Objects.requireNonNull(assetId, "asset_id");
    }
}
