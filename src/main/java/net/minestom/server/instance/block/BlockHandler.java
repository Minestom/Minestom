package net.minestom.server.instance.block;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public abstract class BlockHandler {

    private final Block block;

    public BlockHandler(Block block) {
        this.block = block;
    }

    /**
     * Called when a block has been placed.
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     */
    public abstract void onPlace(@NotNull Instance instance, @NotNull BlockPosition blockPosition);

    /**
     * Called when a block has been destroyed or replaced.
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     */
    public abstract void onDestroy(@NotNull Instance instance, @NotNull BlockPosition blockPosition);

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance).
     *
     * @param player        the player interacting
     * @param hand          the hand used to interact
     * @param blockPosition the position of this block
     * @return true if this block blocks normal item use, false otherwise
     */
    public abstract boolean onInteract(@NotNull Player player, @NotNull Player.Hand hand, @NotNull BlockPosition blockPosition);

    /**
     * Gets the drag of this block.
     * <p>
     * Has to be between 0 and 1.
     *
     * @param instance      the instance of the block
     * @param blockPosition the block position
     * @return the drag to apply
     */
    public float getDrag(@NotNull Instance instance, @NotNull BlockPosition blockPosition) {
        return block.registry().friction();
    }

    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param instance the instance
     * @param position the position at which the block is
     * @param touching the entity currently touching the block
     */
    public void handleContact(@NotNull Instance instance, @NotNull BlockPosition position, @NotNull Entity touching) {
    }

    /**
     * Gets the id of this handler.
     * <p>
     * Used to write the block entity in the anvil world format.
     *
     * @return the namespace id of this handler
     */
    public abstract @NotNull NamespaceID getNamespaceId();
}
