package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.validate.Check;

record TrimPatternImpl(
        Key assetId,
        Component description,
        boolean isDecal
) implements TrimPattern {

    TrimPatternImpl {
        Check.notNull(assetId, "missing asset id");
        Check.notNull(description, "missing description");
    }

}
