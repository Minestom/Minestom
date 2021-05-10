package net.minestom.server.potion;

import org.jetbrains.annotations.NotNull;

public class TimedPotion {

    private final Potion potion;
    private final long startingTime;

    public TimedPotion(@NotNull Potion potion, long startingTime) {
        this.potion = potion;
        this.startingTime = startingTime;
    }

    @NotNull
    public Potion getPotion() {
        return potion;
    }

    public long getStartingTime() {
        return startingTime;
    }
}
