package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;

/**
 * Contains information about a click.
 * For clicked slot IDs, a positive slot indicates the slot ID in the open inventory (which may be the player inventory)
 * while a negative slot ID indicates a click in the player inventory section. The negative slot ID, when negated,
 * becomes the ID of the inventory slot that was selected, so it may be set directly.<br>
 * In maps of changes, negative slot IDs may be used to change player inventory slots.
 */
public sealed interface ClickInfo {

    record LeftClick(int clickedSlot) implements ClickInfo {}
    record RightClick(int clickedSlot) implements ClickInfo {}
    record ShiftClick(int clickedSlot) implements ClickInfo {}
    record DropCursor(boolean all) implements ClickInfo {}
    record DropSlot(int clickedSlot, boolean all) implements ClickInfo {}
    record DistributeCursor(@NotNull IntSet includedSlots, boolean evenlyDistribute) implements ClickInfo {}
    record HotbarSwap(int hotbarSlot, int clickedSlot) implements ClickInfo {}
    record OffhandSwap(int clickedSlot) implements ClickInfo {}
    record DoubleClick(int clickedSlot) implements ClickInfo {}
    record CopyItem(int clickedSlot) implements ClickInfo {}
    record CopyCursor(@NotNull IntSet includedSlots) implements ClickInfo {}

}
