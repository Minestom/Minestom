package net.minestom.server.inventory.click;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A tagged union representing possible clicks from the client.
 */
public sealed interface Click {

    /**
     * Gets the slot of this click. -999 indicates the click either drops the cursor item in some way (implements
     * {@link DropCursor}) or is a drag click, which support multiple slots (implements {@link Drag}). Otherwise, this
     * represents a slot inside the relevant inventory, so {@code inventory.getItemStack(click.slot())}) will return the
     * "clicked" item.
     */
    default int slot() {
        return -999;
    }

    /**
     * Represents the player dropping an item, whether from clicking outside the inventory or from pressing the drop
     * key.
     */
    sealed interface DropCursor extends Click {
    }

    /**
     * Represents a drag click in an inventory.
     */
    sealed interface Drag extends Click {

        /**
         * Returns the list of slots. When the event inventory is the opened inventory, slots greater than its size
         * indicate slots in the player inventory; subtract the size of the event inventory to get the player inventory
         * slot.
         */
        List<Integer> slots();

    }

    record Left(int slot) implements Click {
    }

    record Right(int slot) implements Click {
    }

    record Middle(int slot) implements Click {
        // Creative only
    }

    record LeftShift(int slot) implements Click {
    }

    record RightShift(int slot) implements Click {
    }

    record Double(int slot) implements Click {
    }

    record LeftDrag(List<Integer> slots) implements Drag {
        public LeftDrag {
            slots = List.copyOf(slots);
        }
    }

    record RightDrag(List<Integer> slots) implements Drag {
        public RightDrag {
            slots = List.copyOf(slots);
        }
    }

    record MiddleDrag(List<Integer> slots) implements Drag {
        // Creative only
        public MiddleDrag {
            slots = List.copyOf(slots);
        }
    }

    record LeftDropCursor() implements DropCursor {
    }

    record RightDropCursor() implements DropCursor {
    }

    record MiddleDropCursor() implements DropCursor {
    }

    record DropSlot(int slot, boolean all) implements Click {
    }

    record HotbarSwap(int hotbarSlot, int slot) implements Click {
    }

    record OffhandSwap(int slot) implements Click {
    }

    /**
     * Converts any clicks that are fully within the player inventory into clicks that are considered as being inside
     * the player inventory. This is useful for making click event APIs much less obfuscated due to how the protocol is
     * structured.
     * <br>
     * Essentially, if the player has an inventory open but clicks inside their own inventory, the packet sent will be
     * inside the opened inventory but have a slot ID greater than the size of the opened inventory. For cases where
     * this happens, this function will convert it into a click that's considered inside the player inventory instead,
     * adjusting the slot ID as necessary. On the returned {@link Window} instance, the boolean field indicates which
     * inventory the click is in (since it was unambiguous previously, but is not now).
     *
     * @param click the click to convert
     * @param containerSize the size of the opened container, or null if the player inventory is open
     * @return the (possibly) converted click
     */
    @ApiStatus.Internal
    static Click.Window toWindow(Click click, @Nullable Integer containerSize) {
        return switch (click) {
            // Everything with one dynamic slot
            case Left(int slot) -> toWindowSingle(slot, containerSize, Left::new);
            case Right(int slot) -> toWindowSingle(slot, containerSize, Right::new);
            case Middle(int slot) -> toWindowSingle(slot, containerSize, Middle::new);
            case LeftShift(int slot) -> toWindowSingle(slot, containerSize, LeftShift::new);
            case RightShift(int slot) -> toWindowSingle(slot, containerSize, RightShift::new);
            case Double(int slot) -> toWindowSingle(slot, containerSize, Double::new);
            case OffhandSwap(int slot) -> toWindowSingle(slot, containerSize, OffhandSwap::new);
            case DropSlot(int slot, boolean all) -> toWindowSingle(slot, containerSize, s -> new DropSlot(s, all));
            case HotbarSwap(int hotbarSlot, int slot) -> toWindowSingle(slot, containerSize, s -> new HotbarSwap(hotbarSlot, s));

            // Everything with zero slots
            case LeftDropCursor() -> new Window(false, click);
            case MiddleDropCursor() -> new Window(false, click);
            case RightDropCursor() -> new Window(false, click);

            // Everything with multiple slots
            case LeftDrag(List<Integer> slots) -> toWindowMultiple(slots, containerSize, LeftDrag::new);
            case RightDrag(List<Integer> slots) -> toWindowMultiple(slots, containerSize, RightDrag::new);
            case MiddleDrag(List<Integer> slots) -> toWindowMultiple(slots, containerSize, MiddleDrag::new);
        };
    }

    private static Click.Window toWindowSingle(int slot, @Nullable Integer containerSize, IntFunction<Click> constructor) {
        if (containerSize == null) { // No opened inventory, so always in the player inventory
            return new Window(false, constructor.apply(slot));
        } else if (slot < containerSize) { // In the opened inventory, so do nothing
            return new Window(true, constructor.apply(slot));
        } else { // In the opened inventory, so shift it over and place inside player inventory
            return new Window(false, constructor.apply(slot - containerSize));
        }
    }

    private static Click.Window toWindowMultiple(List<Integer> slots, @Nullable Integer containerSize, Function<List<Integer>, Click> constructor) {
        if (containerSize == null) { // No opened inventory, so always in the player inventory
            return new Window(false, constructor.apply(slots));
        }

        // If there's at least one slot in the opened inventory, the entire click is considered inside it
        for (int slot : slots) {
            if (slot < containerSize) {
                return new Window(true, constructor.apply(slots));
            }
        }

        // Otherwise, everything is in the player inventory, and map it over
        return new Window(false, constructor.apply(slots.stream().map(slot -> slot - containerSize).toList()));
    }

    /**
     * Converts a click from window-specific context back to "normal" click information.
     * <br>
     * This is the inverse of {@link #toWindow(Click, Integer)}; read that for more information
     *
     * @param window the click, along with whether or not it was inside the window
     * @param containerSize the size of the opened container, or null if the player inventory is open
     * @return the (potentially) converted click information
     */
    @ApiStatus.Internal
    static Click fromWindow(Click.Window window, @Nullable Integer containerSize) {
        return switch (window.click()) {
            // Everything with one dynamic slot
            case Left(int slot) -> fromWindowSingle(window, containerSize, Left::new);
            case Right(int slot) -> fromWindowSingle(window, containerSize, Right::new);
            case Middle(int slot) -> fromWindowSingle(window, containerSize, Middle::new);
            case LeftShift(int slot) -> fromWindowSingle(window, containerSize, LeftShift::new);
            case RightShift(int slot) -> fromWindowSingle(window, containerSize, RightShift::new);
            case Double(int slot) -> fromWindowSingle(window, containerSize, Double::new);
            case OffhandSwap(int slot) -> fromWindowSingle(window, containerSize, OffhandSwap::new);
            case DropSlot(int slot, boolean all) -> fromWindowSingle(window, containerSize, s -> new DropSlot(s, all));
            case HotbarSwap(int hotbarSlot, int slot) -> fromWindowSingle(window, containerSize, s -> new HotbarSwap(hotbarSlot, s));

            // Everything with zero slots
            case LeftDropCursor() -> window.click();
            case RightDropCursor() -> window.click();
            case MiddleDropCursor() -> window.click();

            // Everything with multiple slots
            case LeftDrag(List<Integer> slots) -> fromWindowMultiple(window, slots, containerSize, LeftDrag::new);
            case RightDrag(List<Integer> slots) -> fromWindowMultiple(window, slots, containerSize, RightDrag::new);
            case MiddleDrag(List<Integer> slots) -> fromWindowMultiple(window, slots, containerSize, MiddleDrag::new);
        };
    }

    private static Click fromWindowSingle(Click.Window window, @Nullable Integer containerSize, IntFunction<Click> constructor) {
        // The inverse of toWindowSingle; more details there
        return containerSize == null || window.inOpened() ? window.click()
                : constructor.apply(window.click().slot() + containerSize);
    }

    private static Click fromWindowMultiple(Window window, List<Integer> slots, @Nullable Integer containerSize, Function<List<Integer>, Click> constructor) {
        // The inverse of toWindowMultiple; more details there
        return containerSize == null || window.inOpened() ? window.click()
                : constructor.apply(slots.stream().map(slot -> slot + containerSize).toList());
    }

    /**
     * Represents a click inside a window.
     *
     * @param inOpened whether the window is the player inventory (false) or the opened inventory (true).
     * @param click the (contextualized) click
     */
    record Window(boolean inOpened, Click click) {
    }

}
