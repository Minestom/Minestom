package net.minestom.server.instance.block;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;

/**
 * TODO
 * - option to set the global as "global breaking" meaning that multiple players mining the same block will break it faster (cumulation)
 */
public abstract class CustomBlock {

    private short blockId;
    private String identifier;

    public CustomBlock(short blockId, String identifier) {
        this.blockId = blockId;
        this.identifier = identifier;
    }

    // TODO add another object parameter which will offer a lot of integrated features (like break animation, id change etc...)
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
        throw new UnsupportedOperationException("Update method not overridden");
    }

    public abstract void onPlace(Instance instance, BlockPosition blockPosition, Data data);

    public abstract void onDestroy(Instance instance, BlockPosition blockPosition, Data data);

    public abstract void onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data);

    public abstract UpdateOption getUpdateOption();

    /**
     * This id can be serialized in chunk file, meaning no duplicate should exist
     * Changing this value halfway should mean potentially breaking the world
     *
     * @return the custom block id
     */
    public abstract short getCustomBlockId();

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

    public short getBlockId() {
        return blockId;
    }

    public String getIdentifier() {
        return identifier;
    }
}
