package net.minestom.server.item.armor;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.Objects;

record TrimPatternImpl(
        Key assetId,
        Component description,
        boolean isDecal
) implements TrimPattern {

    TrimPatternImpl {
        Objects.requireNonNull(assetId, "missing asset id");
        Objects.requireNonNull(description, "missing description");
    }

}
