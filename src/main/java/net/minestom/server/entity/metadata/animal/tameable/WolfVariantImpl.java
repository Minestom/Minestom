package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.utils.validate.Check;

import java.util.Objects;

record WolfVariantImpl(Assets assets, Assets babyAssets) implements WolfVariant {
    WolfVariantImpl {
        // The builder can violate the nullability constraints
        Objects.requireNonNull(assets, "assets");
        Check.notNull(babyAssets, "babyAssets");
    }
}
