package net.minestom.server.inventory.click;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A tagged union representing possible clicks from the client.
 */
public sealed interface Click {

    /**
     * Gets the slot of this click. -999 indicates the click either drops the cursor item in some way (implements
     * {@link DropCursor}) or is a drag click with multiple slots (implements {@link Drag}).
     */
    default int slot() {
        return -999;
    }

    /**
     * Represents
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
        @NotNull List<Integer> slots();

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

    static @NotNull Click.Window toWindow(@NotNull Click click, @Nullable Integer containerSize) {
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

    private static @NotNull Click.Window toWindowSingle(int slot, @Nullable Integer containerSize, @NotNull IntFunction<Click> constructor) {
        if (containerSize == null) {
            return new Window(false, constructor.apply(slot));
        } else if (slot < containerSize) {
            return new Window(true, constructor.apply(slot));
        } else {
            return new Window(false, constructor.apply(slot - containerSize));
        }
    }

    private static @NotNull Click.Window toWindowMultiple(@NotNull List<Integer> slots, @Nullable Integer containerSize, @NotNull Function<List<Integer>, Click> constructor) {
        if (containerSize == null) {
            return new Window(false, constructor.apply(slots));
        }

        for (int slot : slots) {
            if (slot < containerSize) {
                return new Window(true, constructor.apply(slots));
            }
        }

        return new Window(false, constructor.apply(slots.stream().map(slot -> slot - containerSize).toList()));
    }

    static @NotNull Click fromWindow(@NotNull Click.Window window, @Nullable Integer containerSize) {
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

    private static @NotNull Click fromWindowSingle(@NotNull Click.Window window, @Nullable Integer containerSize, @NotNull IntFunction<Click> constructor) {
        return containerSize == null || window.inOpened() ? window.click()
                : constructor.apply(window.click().slot() + containerSize);
    }

    private static @NotNull Click fromWindowMultiple(@NotNull Window window, @NotNull List<Integer> slots, @Nullable Integer containerSize, @NotNull Function<List<Integer>, Click> constructor) {
        return containerSize == null || window.inOpened() ? window.click()
                : constructor.apply(slots.stream().map(slot -> slot + containerSize).toList());
    }

    /**
     * Represents a click inside a window.
     *
     * @param inOpened whether the window is the player inventory (false) or the opened inventory (true).
     * @param click the (contextualized) click
     */
    record Window(boolean inOpened, @NotNull Click click) {
    }

}
