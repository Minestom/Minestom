package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.instance.BlockModifier;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represent the handler of a custom block type.
 * <p>
 * There should be only one instance of this class for each custom block type,
 * every individual blocks will execute the callbacks present there. Each of which contains the
 * custom block position and the instance concerned
 */
public abstract class CustomBlock {

    public static final byte MAX_STAGE = 10;

    /**
     * Instance -> break data
     * Used to store block break stage data when {@link #enableMultiPlayerBreaking()} is enabled
     */
    private final Map<Instance, InstanceBreakData> instanceBreakDataMap = new HashMap<>();

    public int getBreakEntityId(Player firstBreaker) {
        return firstBreaker.getEntityId() + 1;
    }

    private final short defaultBlockStateId;
    private final String identifier;

    /**
     * @param defaultBlockStateId the default block state id
     * @param identifier          the custom block identifier
     */
    public CustomBlock(short defaultBlockStateId, String identifier) {
        this.defaultBlockStateId = defaultBlockStateId;
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
     * Called when the player requests the next stage break delay
     *
     * @param player   the player who is trying to break the block
     * @param position the block position
     * @param stage    the current break stage of the block (0-10)
     * @param breakers the list containing all the players currently digging this block
     * @return the time in tick to pass to the next state, 0 to instant break it.
     * negative value allow to skip stages (-2 will skip 2 stages per tick)
     * @see #enableCustomBreakDelay() to enable/disable it
     */
    public int getBreakDelay(Player player, BlockPosition position, byte stage, Set<Player> breakers) {
        return 0;
    }

    /**
     * Used to enable the custom break delay from {@link #getBreakDelay(Player, BlockPosition, byte, Set)}
     * Disabling it would result in having vanilla time
     *
     * @return true to enable custom break delay
     */
    public boolean enableCustomBreakDelay() {
        return false;
    }

    /**
     * Get if this block breaking time can be reduced by having multiple players
     * digging it
     * <p>
     * WARNING: this should be constant, do not change this value halfway
     *
     * @return true to enable the multi-player breaking feature
     */
    public boolean enableMultiPlayerBreaking() {
        return false;
    }

    /**
     * Get if this {@link CustomBlock} requires any tick update
     *
     * @return true if {@link #getUpdateOption()} is not null and the value is positive
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
     * @param instance the instance
     * @param position the position at which the block is
     * @param touching the entity currently touching the block
     */
    public void handleContact(Instance instance, BlockPosition position, Entity touching) {
    }

    /**
     * This is the default block state id when the custom block is set,
     * it is possible to change this value per block using
     * {@link BlockModifier#setSeparateBlocks(int, int, int, short, short)}
     * <p>
     * Meaning that you should not believe that your custom blocks id will always be this one.
     *
     * @return the default visual block id
     */
    public short getDefaultBlockStateId() {
        return defaultBlockStateId;
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
     * Get the drag of this block
     * <p>
     * It has to be between 0 and 1
     *
     * @return the drag of this block
     */
    public float getDrag(Instance instance, BlockPosition blockPosition) {
        return 0.5f;
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
     * @param instance           the instance
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
     * @param tableManager the loot table manager
     * @return the loot table associated to this block
     */
    public LootTable getLootTable(LootTableManager tableManager) {
        return null;
    }

    // BLOCK BREAK METHODS

    /**
     * Called when a player start digging this custom block,
     * process all necessary data if {@link #enableMultiPlayerBreaking()} is enabled
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     * @param player        the player who started digging
     */
    public void startDigging(Instance instance, BlockPosition blockPosition, Player player) {
        // Stay null if multi player breaking is disabled
        Set<Player> breakers = null;

        if (enableMultiPlayerBreaking()) {
            // Multi player breaking enabled, get the breakers and cache some values
            InstanceBreakData instanceBreakData = instanceBreakDataMap.computeIfAbsent(instance, i -> new InstanceBreakData());

            Map<BlockPosition, Set<Player>> breakersMap = instanceBreakData.breakersMap;
            breakers = breakersMap.computeIfAbsent(blockPosition, pos -> new HashSet<>(1));
            breakers.add(player);

            Object2ByteMap<BlockPosition> breakStageMap = instanceBreakData.breakStageMap;
            // Set the block stage to 0, use the previous one if any
            if (!breakStageMap.containsKey(blockPosition)) {
                breakStageMap.put(blockPosition, (byte) 0);
            }

            Object2IntMap<BlockPosition> breakIdMap = instanceBreakData.breakIdMap;
            // Set the entity id used for the packet, otherwise use the previous one
            if (!breakIdMap.containsKey(blockPosition)) {
                breakIdMap.put(blockPosition, getBreakEntityId(player));
            }
        }

        // Set the player target block
        player.setTargetBlock(this, blockPosition, breakers);
    }

    /**
     * Called when a player stop digging a block,
     * does remove the block break animation if he was the only breaker
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     * @param player        the player who stopped digging
     */
    public void stopDigging(Instance instance, BlockPosition blockPosition, Player player) {
        if (enableMultiPlayerBreaking()) {
            // Remove cache data
            if (instanceBreakDataMap.containsKey(instance)) {
                InstanceBreakData instanceBreakData = instanceBreakDataMap.get(instance);

                Set<Player> breakers = instanceBreakData.breakersMap.get(blockPosition);
                if (breakers != null) {
                    breakers.remove(player);
                    if (breakers.isEmpty()) {
                        // No remaining breakers

                        // Get the entity id assigned to the block break
                        final int entityId = instanceBreakData.breakIdMap.getInt(blockPosition);

                        final Chunk chunk = instance.getChunkAt(blockPosition);
                        chunk.sendPacketToViewers(new BlockBreakAnimationPacket(entityId, blockPosition, (byte) -1));

                        // Clear cache
                        removeDiggingInformation(instance, blockPosition);
                    }
                }

            }
        } else {
            // Stop the breaking animation for the specific player id
            final Chunk chunk = instance.getChunkAt(blockPosition);
            final int entityId = getBreakEntityId(player);
            chunk.sendPacketToViewers(new BlockBreakAnimationPacket(entityId, blockPosition, (byte) -1));
        }
    }

    /**
     * Process one stage on the block, break it if it excess {@link #MAX_STAGE},
     * only if {@link #enableMultiPlayerBreaking()} is enabled
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     * @param player        the player who processed one stage on the block
     * @param stageIncrease the number of stage increase
     * @return true if the block can continue being digged
     * @throws IllegalStateException if {@link #enableMultiPlayerBreaking()} is disabled
     */
    public synchronized boolean processStage(Instance instance, BlockPosition blockPosition, Player player, byte stageIncrease) {
        Check.stateCondition(!enableMultiPlayerBreaking(),
                "CustomBlock#processState requires having the multi player breaking feature enabled");

        if (instanceBreakDataMap.containsKey(instance)) {
            InstanceBreakData instanceBreakData = instanceBreakDataMap.get(instance);
            Object2ByteMap<BlockPosition> breakStageMap = instanceBreakData.breakStageMap;
            byte stage = breakStageMap.getByte(blockPosition);
            if (stage + stageIncrease >= MAX_STAGE) {
                instance.breakBlock(player, blockPosition);
                return false;
            } else {

                // Get the entity id assigned to the block break
                final int entityId = instanceBreakData.breakIdMap.getInt(blockPosition);

                // Send the block break animation
                final Chunk chunk = instance.getChunkAt(blockPosition);
                chunk.sendPacketToViewers(new BlockBreakAnimationPacket(entityId, blockPosition, stage));

                // Refresh the stage
                stage += stageIncrease;
                breakStageMap.put(blockPosition, stage);
                return true;
            }
        }
        return false;
    }

    public void removeDiggingInformation(Instance instance, BlockPosition blockPosition) {
        if (!enableMultiPlayerBreaking()) {
            return;
        }

        if (instanceBreakDataMap.containsKey(instance)) {
            InstanceBreakData instanceBreakData = instanceBreakDataMap.get(instance);
            // Remove the block position from all maps
            instanceBreakData.clear(blockPosition);
        }
    }

    /**
     * Get all the breakers of a block, only if {@link #enableMultiPlayerBreaking()} is enabled
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     * @return the {@link Set} of breakers of a block
     * @throws IllegalStateException if {@link #enableMultiPlayerBreaking()} is disabled
     */
    public Set<Player> getBreakers(Instance instance, BlockPosition blockPosition) {
        Check.stateCondition(!enableMultiPlayerBreaking(),
                "CustomBlock#getBreakers requires having the multi player breaking feature enabled");

        if (instanceBreakDataMap.containsKey(instance)) {
            InstanceBreakData instanceBreakData = instanceBreakDataMap.get(instance);
            return instanceBreakData.breakersMap.get(blockPosition);
        }
        return null;
    }

    /**
     * Get the block break stage at a position, only work if {@link #enableMultiPlayerBreaking()} is enabled
     *
     * @param instance      the instance of the custom block
     * @param blockPosition the position of the custom block
     * @return the break stage at the position. Can also be 0 when nonexistent
     */
    public byte getBreakStage(Instance instance, BlockPosition blockPosition) {
        Check.stateCondition(!enableMultiPlayerBreaking(),
                "CustomBlock#getBreakStage requires having the multi player breaking feature enabled");

        if (!instanceBreakDataMap.containsKey(instance))
            return 0;
        final InstanceBreakData instanceBreakData = instanceBreakDataMap.get(instance);
        return instanceBreakData.breakStageMap.getByte(blockPosition);
    }

    /**
     * Class used to store block break stage
     * Only used if multi player breaking is enabled
     */
    private static class InstanceBreakData {
        // Contains all the breakers of a block
        private final Map<BlockPosition, Set<Player>> breakersMap = new HashMap<>();
        // Contains the current break stage of a block
        private final Object2ByteMap<BlockPosition> breakStageMap = new Object2ByteOpenHashMap<>();
        // Contains the entity id used by the block break packet
        private final Object2IntMap<BlockPosition> breakIdMap = new Object2IntOpenHashMap<>();

        private void clear(BlockPosition blockPosition) {
            this.breakersMap.remove(blockPosition);
            this.breakStageMap.removeByte(blockPosition);
            this.breakIdMap.removeInt(blockPosition);
        }

    }
}
