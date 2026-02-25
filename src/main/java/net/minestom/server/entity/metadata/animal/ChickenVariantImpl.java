package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;

record ChickenVariantImpl(
        ChickenVariant.Model model,
        Key assetId
) implements ChickenVariant {

    public ChickenVariantImpl {
        Check.notNull(model, "Model cannot be null");
        Check.notNull(assetId, "Asset ID cannot be null");
    }
}
