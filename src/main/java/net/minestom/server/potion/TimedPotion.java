package net.minestom.server.potion;

public class TimedPotion {
    private final Potion potion;
    private final long startingTime;

    public TimedPotion(Potion potion, long startingTime) {
        this.potion = potion;
        this.startingTime = startingTime;
    }

    public Potion getPotion() {
        return potion;
    }

    public long getStartingTime() {
        return startingTime;
    }
}
