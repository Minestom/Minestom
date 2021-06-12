package net.minestom.server.instance.block;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to provide block behavior. Set with {@link Block#withHandler(BlockHandler)}.
 * <p>
 * Implementations are expected to be thread safe.
 */
public interface BlockHandler {

    /**
     * Called when a block has been placed.
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     */
    void onPlace(@NotNull Instance instance, @NotNull BlockPosition blockPosition);

    /**
     * Called when a block has been destroyed or replaced.
     *
     * @param instance      the instance of the block
     * @param blockPosition the position of the block
     */
    void onDestroy(@NotNull Instance instance, @NotNull BlockPosition blockPosition);

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance).
     *
     * @param player        the player interacting
     * @param hand          the hand used to interact
     * @param blockPosition the position of this block
     * @return true if this block blocks normal item use, false otherwise
     */
    boolean onInteract(@NotNull Player player, @NotNull Player.Hand hand, @NotNull BlockPosition blockPosition);

    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param instance the instance
     * @param position the position at which the block is
     * @param touching the entity currently touching the block
     */
    default void handleContact(@NotNull Instance instance, @NotNull BlockPosition position, @NotNull Entity touching) {
    }

    /**
     * Gets the id of this handler.
     * <p>
     * Used to write the block entity in the anvil world format.
     *
     * @return the namespace id of this handler
     */
    @NotNull NamespaceID getNamespaceId();
}
