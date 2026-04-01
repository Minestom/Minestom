package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;

import java.util.Objects;

public record PigVariantImpl(
        PigVariant.Model model,
        Key assetId,
        Key babyAssetId
) implements PigVariant {

    public PigVariantImpl {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(babyAssetId, "babyAssetId");
    }
}
