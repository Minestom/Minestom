package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

/**
 * Contains information about a click. These are equal to the packet slot IDs from <a href="https://wiki.vg/Inventory">the Minecraft protocol.</a>.
 * The inventory used should be known from context.
 */
public sealed interface ClickInfo {

    record LeftClick(int clickedSlot) implements ClickInfo {}
    record RightClick(int clickedSlot) implements ClickInfo {}
    record ShiftClick(int clickedSlot) implements ClickInfo {}
    record DropCursor(boolean all) implements ClickInfo {}
    record DropSlot(int clickedSlot, boolean all) implements ClickInfo {}
    record DragClick(@NotNull IntList includedSlots, boolean evenlyDistribute) implements ClickInfo {}
    record HotbarSwap(int hotbarSlot, int clickedSlot) implements ClickInfo {}
    record OffhandSwap(int clickedSlot) implements ClickInfo {}
    record DoubleClick(int clickedSlot) implements ClickInfo {}
    record CopyItem(int clickedSlot) implements ClickInfo {}
    record CopyCursor(@NotNull IntList includedSlots) implements ClickInfo {}

}
