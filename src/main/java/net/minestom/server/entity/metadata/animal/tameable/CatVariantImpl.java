package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;

record CatVariantImpl(Key assetId) implements CatVariant {

    public CatVariantImpl {
        Check.notNull(assetId, "assetId");
    }
}
