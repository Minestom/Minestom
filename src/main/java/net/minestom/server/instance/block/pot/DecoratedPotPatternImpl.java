package net.minestom.server.instance.block.pot;

import net.kyori.adventure.key.Key;

import java.util.Objects;

record DecoratedPotPatternImpl(Key assetId) implements DecoratedPotPattern {
    DecoratedPotPatternImpl {
        Objects.requireNonNull(assetId, "assetId");
    }
}
