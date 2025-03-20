package net.minestom.server.event.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class InstanceBlockChangeEvent implements Event, BlockEvent, CancellableEvent, InstanceEvent {
    // The source of the block change event (e.g., player, instance)
    private final BlockEvent.Source source;

    // The position of the block that is being changed
    private final BlockVec position;
    // The block that was previously at the given position
    private final Block previousBlock;
    // The new block that will replace the existing block
    private Block newBlock;
    // Flag to determine if the event is cancelled
    private boolean cancelled = false;

    // Flag to determine if block updates should be triggered
    private boolean doBlockUpdates = true;

    // Flag to determine if the action consumes an item (e.g., placing a block removes it from inventory)
    private boolean doesConsumeItem = true;

    /**
     * Constructs an InstanceBlockChangeEvent.
     *
     * @param source        The source of the block change event.
     * @param newBlock      The new block that is being placed.
     * @param previousBlock The block that is being replaced.
     * @param position      The position of the block change.
     */
    public InstanceBlockChangeEvent(@NotNull BlockEvent.Source source,
                                    @NotNull Block newBlock,
                                    @NotNull Block previousBlock,
                                    @NotNull BlockVec position) {
        this.source = source;
        this.newBlock = newBlock;
        this.previousBlock = previousBlock;
        this.position = position;
    }

    /**
     * @return The source that caused this block change.
     */
    @Override
    public @NotNull Source getSource() {
        return source;
    }

    /**
     * @return The instance (world) where the block change is occurring.
     */
    @Override
    public @NotNull Instance getInstance() {
        return source.instance();
    }

    /**
     * @return The new block that is set to replace the previous block.
     */
    @Override
    public @NotNull Block getBlock() {
        return newBlock;
    }

    /**
     * Sets the new block that will replace the previous block.
     *
     * @param block The new block to set.
     */
    public void setBlock(Block block) {
        this.newBlock = block;
    }

    /**
     * @return The block that was previously at this position.
     */
    public @NotNull Block getPreviousBlock() {
        return previousBlock;
    }

    /**
     * @return The position where the block change is taking place.
     */
    @Override
    public @NotNull BlockVec getBlockPosition() {
        return position;
    }

    /**
     * @return True if the event has been cancelled, preventing the block change.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the event is cancelled, preventing the block change.
     *
     * @param cancel True to cancel the event, false otherwise.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * @return True if the block change was caused by a player.
     */
    public boolean fromPlayer() {
        return source instanceof Source.Player;
    }

    /**
     * @return True if the event represents a block being broken (replaced with air).
     */
    public boolean isBlockBreak() {
        return getBlock().isAir();
    }

    /**
     * @return True if block updates should be triggered after the block change.
     */
    public boolean doBlockUpdates() {
        return doBlockUpdates;
    }

    /**
     * @return True if the block change consumes an item from the player's inventory.
     */
    public boolean doesConsumeItem() {
        return doesConsumeItem;
    }

    /**
     * Sets whether block updates should be triggered after the block change.
     *
     * @param doBlockUpdates True to allow updates, false to prevent them.
     */
    public void doBlockUpdates(boolean doBlockUpdates) {
        this.doBlockUpdates = doBlockUpdates;
    }

    /**
     * Sets whether the block change consumes an item (e.g., placing a block removes it from inventory).
     *
     * @param doesConsumeItem True if the action should consume an item, false otherwise.
     */
    public void doesConsumeItem(boolean doesConsumeItem) {
        this.doesConsumeItem = doesConsumeItem;
    }
}