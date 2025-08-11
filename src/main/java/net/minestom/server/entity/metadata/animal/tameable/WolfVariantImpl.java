package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.utils.validate.Check;

record WolfVariantImpl(Assets assets) implements WolfVariant {
    WolfVariantImpl {
        // The builder can violate the nullability constraints
        Check.notNull(assets, "missing assets Asset");
    }
}
