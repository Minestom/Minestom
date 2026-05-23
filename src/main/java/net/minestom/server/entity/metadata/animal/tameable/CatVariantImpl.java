package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;

import java.util.Objects;

record CatVariantImpl(Key assetId, Key babyAssetId) implements CatVariant {

    public CatVariantImpl {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(babyAssetId, "babyAssetId");
    }
}
