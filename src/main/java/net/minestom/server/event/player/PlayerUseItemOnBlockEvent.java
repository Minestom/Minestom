package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Used when a player is clicking on a block with an item (but is not a block in item form).
 */
public record PlayerUseItemOnBlockEvent(@NotNull Player player, @NotNull PlayerHand hand,
                                        @NotNull ItemStack itemStack,
                                        @NotNull Point position, @NotNull Point cursorPosition,
                                        @NotNull BlockFace blockFace) implements PlayerInstanceEvent, ItemEvent {

    /**
     * Gets the position of the interacted block.
     *
     * @return the block position
     */
    @Override
    public @NotNull Point position() {
        return position;
    }

    /**
     * Gets the cursor position of the player when interacting with the block.
     *
     * @return the cursor position
     */
    @Override
    public @NotNull Point cursorPosition() {
        return cursorPosition;
    }

    /**
     * Gets which face the player has interacted with.
     *
     * @return the block face
     */
    @Override
    public @NotNull BlockFace blockFace() {
        return blockFace;
    }

    /**
     * Gets which hand the player used to interact with the block.
     *
     * @return the hand
     */
    @Override
    public @NotNull PlayerHand hand() {
        return hand;
    }

    /**
     * Gets with which item the player has interacted with the block.
     *
     * @return the item
     */
    @Override
    public @NotNull ItemStack itemStack() {
        return itemStack;
    }
}
