package net.minestom.server.instance.block;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represent the handler of a custom block type.
 * <p>
 * There should be only one instance of this class for each custom block type,
 * every individual blocks will execute the callbacks present there. Each of which contains the
 * custom block position and the instance concerned
 */
public abstract class CustomBlock {

    /**
     * TODO
     * - option to set the global as "global breaking" meaning that multiple players mining the same block will break it faster (accumulation)
     */

    private final short blockId;
    private final String identifier;

    /**
     * @param blockId    the visual block id
     * @param identifier the custom block identifier
     */
    public CustomBlock(short blockId, String identifier) {
        this.blockId = blockId;
        this.identifier = identifier;
    }

    public CustomBlock(Block block, String identifier) {
        this(block.getBlockId(), identifier);
    }

    /**
     * Calling delay depends on {@link #getUpdateOption()} which should be overridden
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     * @param data          the data associated with the block
     * @throws UnsupportedOperationException if {@link #getUpdateOption()}
     *                                       is not null but the update method is not overridden
     */
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
        throw new UnsupportedOperationException("Update method not overridden");
    }

    /**
     * The update option is used to define the delay between two
     * {@link #update(Instance, BlockPosition, Data)} execution.
     * <p>
     * If this is not null, {@link #update(Instance, BlockPosition, Data)}
     * should be overridden or errors with occurs
     *
     * @return the update option of the block
     */
    public UpdateOption getUpdateOption() {
        return null;
    }

    /**
     * Called when a custom block has been placed
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     * @param data          the data associated with the block
     */
    public abstract void onPlace(Instance instance, BlockPosition blockPosition, Data data);

    /**
     * Called when a custom block has been destroyed or replaced
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     * @param data          the data associated with the block
     */
    public abstract void onDestroy(Instance instance, BlockPosition blockPosition, Data data);

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance)
     *
     * @param player        the player interacting
     * @param hand          the hand used to interact
     * @param blockPosition the position of this block
     * @param data          the data at this position
     * @return true if this block blocks normal item use, false otherwise
     */
    public abstract boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data);

    /**
     * This id can be serialized in chunk file, meaning no duplicate should exist
     * Changing this value halfway should mean potentially breaking the world
     *
     * @return the custom block id
     */
    public abstract short getCustomBlockId();

    /**
     * Called at digging start to check for custom breaking time
     * Can be set to < 0 to be cancelled, in this case vanilla time will be used
     *
     * @param player   the player who is trying to break the block
     * @param position
     * @return the time in ms to break it
     */
    public abstract int getBreakDelay(Player player, BlockPosition position);

    /**
     * @return true if {@link #getUpdateOption()} is not null, false otherwise
     */
    public boolean hasUpdate() {
        final UpdateOption updateOption = getUpdateOption();
        if (updateOption == null)
            return false;

        return updateOption.getValue() > 0;
    }


    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param instance
     * @param position the position at which the block is
     * @param touching the entity currently touching the block
     */
    public void handleContact(Instance instance, BlockPosition position, Entity touching) {
    }

    /**
     * This is the default visual for the block when the custom block is set,
     * it is possible to change this value per block using
     * {@link net.minestom.server.instance.BlockModifier#setSeparateBlocks(int, int, int, short, short)}
     * <p>
     * Meaning that you should not believe that your custom blocks id will always be this one.
     *
     * @return the default visual block id
     */
    public short getBlockId() {
        return blockId;
    }

    /**
     * The custom block identifier, used to retrieve the custom block object with
     * {@link BlockManager#getCustomBlock(String)} and to set custom block in the instance
     *
     * @return the custom block identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Initialises data for this block
     *
     * @param blockPosition the position of the targeted block
     * @param data          data given to 'setBlock', can be null
     * @return Data for this block. Can be null, 'data', or a new object
     */
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        return data;
    }

    /**
     * Update this block from a neighbor. By default calls 'update' if directNeighbor is true
     *
     * @param instance         current instance
     * @param thisPosition     this block's position
     * @param neighborPosition the neighboring block which triggered the update
     * @param directNeighbor   is the neighbor directly connected to this block? (No diagonals)
     */
    public void updateFromNeighbor(Instance instance, BlockPosition thisPosition, BlockPosition neighborPosition, boolean directNeighbor) {
        if (directNeighbor && hasUpdate()) {
            update(instance, thisPosition, instance.getBlockData(thisPosition));
        }
    }

    /**
     * Called when a scheduled update on this block happens. By default, calls 'update'
     *
     * @param instance  the instance of the block
     * @param position  the position of the block
     * @param blockData the data of the block
     */
    public void scheduledUpdate(Instance instance, BlockPosition position, Data blockData) {
        update(instance, position, blockData);
    }

    /**
     * Allows custom block to write block entity data to a given NBT compound.
     * Used to send block entity data to the client over the network.
     * Can also be used to save block entity data on disk for compatible chunk savers
     *
     * @param position  position of the block
     * @param blockData equivalent to <pre>instance.getBlockData(position)</pre>
     */
    public void writeBlockEntity(BlockPosition position, Data blockData, NBTCompound nbt) {
    }

    /**
     * Called when an explosion wants to destroy this block.
     *
     * @param instance
     * @param lootTableArguments arguments used in the loot table loot generation
     * @return 'true' if the explosion should happen on this block, 'false' to cancel the destruction.
     * Returning true does NOT block the explosion rays, ie it does not change the block explosion resistance
     */
    public boolean onExplode(Instance instance, BlockPosition position, Data lootTableArguments) {
        return true;
    }

    /**
     * Return the loot table associated to this block. Return null to use vanilla behavior
     *
     * @param tableManager
     * @return the loot table associated to this block
     */
    public LootTable getLootTable(LootTableManager tableManager) {
        return null;
    }
}
