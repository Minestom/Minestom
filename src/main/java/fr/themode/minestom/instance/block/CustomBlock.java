package fr.themode.minestom.instance.block;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 *  - option to set the global as "global breaking" meaning that multiple players mining the same block will break it faster (cumulation)
 */
public abstract class CustomBlock {

    private static final AtomicInteger idCounter = new AtomicInteger();

    private short id;

    public CustomBlock() {
        this.id = (short) idCounter.incrementAndGet();
    }

    public void update(Data data) {
        throw new UnsupportedOperationException("Update method not overriden");
    }

    public abstract UpdateOption getUpdateOption();

    public abstract short getType();

    public abstract String getIdentifier();

    /*
      Time in ms
     */
    public abstract int getBreakDelay(Player player);

    public boolean hasUpdate() {
        return getUpdateOption().getValue() > 0;
    }

    public short getId() {
        return id;
    }
}
