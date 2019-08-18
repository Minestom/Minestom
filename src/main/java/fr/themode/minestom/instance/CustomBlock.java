package fr.themode.minestom.instance;

import fr.themode.minestom.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class CustomBlock {

    private static final AtomicInteger idCounter = new AtomicInteger();

    private int id;

    public CustomBlock() {
        this.id = idCounter.incrementAndGet();
    }

    public abstract short getType();

    public abstract String getIdentifier();

    /*
      Time in ms
     */
    public abstract int getBreakDelay(Player player);

    public int getId() {
        return id;
    }
}
