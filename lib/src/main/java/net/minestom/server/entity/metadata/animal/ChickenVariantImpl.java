package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;

import java.util.Objects;

record ChickenVariantImpl(
        ChickenVariant.Model model,
        Key assetId,
        Key babyAssetId
) implements ChickenVariant {

    public ChickenVariantImpl {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(babyAssetId, "babyAssetId");
    }
}
