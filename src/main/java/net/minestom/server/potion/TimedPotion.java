package net.minestom.server.potion;

import org.jetbrains.annotations.NotNull;

public record TimedPotion(Potion potion, long startingTicks) {
    @Override
    public @NotNull Potion potion() {
        return potion;
    }
}
