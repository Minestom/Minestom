package net.minestom.server.potion;

public class TimedPotion {
    private final Potion potion;
    private final Long startingTime;

    public TimedPotion(Potion potion, Long startingTime) {
        this.potion = potion;
        this.startingTime = startingTime;
    }

    public Potion getPotion() {
        return potion;
    }

    public Long getStartingTime() {
        return startingTime;
    }
}
