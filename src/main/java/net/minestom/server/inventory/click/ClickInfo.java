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
    record ShiftClick(int clickedSlot, Button button) implements ClickInfo {

        public ShiftClick(int clickedSlot) {
            this(clickedSlot, Button.PRIMARY);
        }

        /**
         * The button used to shift click. {@link Button#PRIMARY} usually indicates a left click, while {@link Button#SECONDARY}
         * usually indicates a right click.
         */
        public enum Button {
            PRIMARY, SECONDARY
        }
    }
    record DropCursor(boolean all) implements ClickInfo {}
    record DropSlot(int clickedSlot, boolean all) implements ClickInfo {}
    record DragClick(@NotNull IntList includedSlots, boolean evenlyDistribute) implements ClickInfo {}
    record HotbarSwap(int hotbarSlot, int clickedSlot) implements ClickInfo {}
    record OffhandSwap(int clickedSlot) implements ClickInfo {}
    record DoubleClick(int clickedSlot) implements ClickInfo {}
    record CopyItem(int clickedSlot) implements ClickInfo {}
    record CopyCursor(@NotNull IntList includedSlots) implements ClickInfo {}

}
