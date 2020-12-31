package net.minestom.server.potion;

public class TimedPotion {
    public Potion potion;
    public Long startingTime;

    public TimedPotion(Potion potion, Long startingTime) {
        this.potion = potion;
        this.startingTime = startingTime;
    }
}
