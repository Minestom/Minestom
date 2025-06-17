package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

record WolfVariantImpl(@NotNull Assets assets) implements WolfVariant {
    WolfVariantImpl {
        // The builder can violate the nullability constraints
        Check.notNull(assets, "missing assets Asset");
    }
}
