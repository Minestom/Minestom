package fr.themode.minestom.instance;

import fr.themode.minestom.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 * option to set the global as "global breaking" meaning that multiple players mining the same block will break it faster (cumulation)
 */
public abstract class CustomBlock {

    private static final AtomicInteger idCounter = new AtomicInteger();

    private short id;

    public CustomBlock() {
        this.id = (short) idCounter.incrementAndGet();
    }

    public abstract short getType();

    public abstract String getIdentifier();

    /*
      Time in ms
     */
    public abstract int getBreakDelay(Player player);

    public short getId() {
        return id;
    }
}
