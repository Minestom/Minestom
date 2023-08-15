package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

/**
 * Contains information about a click.<br>
 * For clicked slot IDs, a value above {@link ClickPreprocessor#PLAYER_INVENTORY_OFFSET} indicates a slot in the
 * player's inventory while another inventory may be open, while otherwise it just represents a slot in the opened
 * inventory. This means that, when the player has just their inventory opened, slot IDs should not be using this
 * offset.
 */
public sealed interface ClickInfo {

    record LeftClick(int clickedSlot) implements ClickInfo {}
    record RightClick(int clickedSlot) implements ClickInfo {}
    record ShiftClick(int clickedSlot) implements ClickInfo {}
    record DropCursor(boolean all) implements ClickInfo {}
    record DropSlot(int clickedSlot, boolean all) implements ClickInfo {}
    record DistributeCursor(@NotNull IntList includedSlots, boolean evenlyDistribute) implements ClickInfo {}
    record HotbarSwap(int hotbarSlot, int clickedSlot) implements ClickInfo {}
    record OffhandSwap(int clickedSlot) implements ClickInfo {}
    record DoubleClick(int clickedSlot) implements ClickInfo {}
    record CopyItem(int clickedSlot) implements ClickInfo {}
    record CopyCursor(@NotNull IntList includedSlots) implements ClickInfo {}

}
