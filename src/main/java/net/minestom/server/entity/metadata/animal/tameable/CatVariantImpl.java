package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

record CatVariantImpl(@NotNull Key assetId) implements CatVariant {

    public CatVariantImpl {
        Check.notNull(assetId, "assetId");
    }
}
