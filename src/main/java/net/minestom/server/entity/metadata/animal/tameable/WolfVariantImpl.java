package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.key.Key;

import java.util.Objects;

record WolfVariantImpl(Assets assets, Assets babyAssets) implements WolfVariant {
    WolfVariantImpl {
        // The builder can violate the nullability constraints
        Objects.requireNonNull(assets, "assets");
        Objects.requireNonNull(babyAssets, "babyAssets");
    }

    record AssetsImpl(Key wild, Key tame, Key angry) implements WolfVariant.Assets {
        public AssetsImpl {
            Objects.requireNonNull(wild, "wild");
            Objects.requireNonNull(tame, "tame");
            Objects.requireNonNull(angry, "angry");
        }
    }
}
