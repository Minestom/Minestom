package fr.themode.minestom.instance.block;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.time.UpdateOption;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 * - option to set the global as "global breaking" meaning that multiple players mining the same block will break it faster (cumulation)
 */
public abstract class CustomBlock {

    private static final AtomicInteger idCounter = new AtomicInteger();

    private short type;
    private String identifier;
    private short id;

    public CustomBlock(short type, String identifier) {
        this.type = type;
        this.identifier = identifier;
        this.id = (short) idCounter.incrementAndGet();
    }

    // TODO add another object parameter which will offer a lot of integrated features (like break animation, id change etc...)
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
        throw new UnsupportedOperationException("Update method not overridden");
    }

    public abstract UpdateOption getUpdateOption();

    /*
      Time in ms
     */
    public abstract int getBreakDelay(Player player);

    public boolean hasUpdate() {
        UpdateOption updateOption = getUpdateOption();
        if (updateOption == null)
            return false;

        return updateOption.getValue() > 0;
    }

    public short getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public short getId() {
        return id;
    }
}
