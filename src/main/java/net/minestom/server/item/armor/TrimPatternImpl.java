package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record TrimPatternImpl(
        @NotNull Key assetId,
        @NotNull Component description,
        boolean isDecal,
        @Nullable Registry.TrimPatternEntry registry
) implements TrimPattern {

    TrimPatternImpl {
        Check.notNull(assetId, "missing asset id");
        Check.notNull(description, "missing description");
    }

    TrimPatternImpl(@NotNull Registry.TrimPatternEntry registry) {
        this(registry.assetID(), registry.description(), registry.decal(), registry);
    }
}
