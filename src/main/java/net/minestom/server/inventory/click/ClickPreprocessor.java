package net.minestom.server.inventory.click;

import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Preprocesses click packets, turning them into {@link Click} instances for further processing.
 */
public final class ClickPreprocessor {
    private final Set<Integer> leftDrag = new LinkedHashSet<>();
    private final Set<Integer> rightDrag = new LinkedHashSet<>();
    private final Set<Integer> middleDrag = new LinkedHashSet<>();

    public void clearCache() {
        this.leftDrag.clear();
        this.rightDrag.clear();
        this.middleDrag.clear();
    }

    /**
     * Determines whether or not a click is creative only. This should match client behaviour, including edge cases like
     * middle clicks (item clones) being sent in survival when there is an item in the cursor (which would make it a
     * no-op), hence the parameter. This function can be overridden if modifying the creative check logic is desired,
     * since {@link net.minestom.server.listener.WindowListener} directly depends on this.
     *
     * @param click the click to check
     * @param hasCursorItem if the client has an item in the cursor (for checking {@code Click.Middle})
     * @return if the click is creative only
     */
    public boolean isCreativeClick(Click click, boolean hasCursorItem) {
        return switch (click) {
            case Click.Middle ignored -> !hasCursorItem; // Block clones (except the edge case)
            case Click.MiddleDrag ignored -> true; // Block clone drags
            default -> false;
        };
    }

    /**
     * Processes the provided click packet, turning it into a {@link Click}.
     *
     * @param packet        the raw click packet
     * @param containerSize the size of the open container, or null if the player inventory is open
     * @return the processed click, or nothing if the click takes place over multiple packets and this is not the final
     *         one (e.g. a drag)
     */
    public @Nullable Click processClick(ClientClickWindowPacket packet, @Nullable Integer containerSize) {
        final int slot;
        if (containerSize == null) {
            slot = PlayerInventoryUtils.convertWindow0SlotToMinestomSlot(packet.slot());
        } else if (packet.slot() >= containerSize) {
            slot = containerSize + PlayerInventoryUtils.convertWindowSlotToMinestomSlot(packet.slot(), containerSize);
        } else {
            slot = packet.slot();
        }

        final int maxSize = containerSize == null ? PlayerInventory.INVENTORY_SIZE : containerSize + PlayerInventory.INNER_INVENTORY_SIZE;
        final boolean valid = slot >= 0 && slot < maxSize;

        if (valid) {
            return process(packet.clickType(), slot, packet.button());
        } else {
            return slot == -999 ? processInvalidSlot(packet.clickType(), packet.button()) : null;
        }
    }

    /**
     * Processes a click in an invalid slot (i.e. the slot is irrelevant, like in a drop)
     */
    private @Nullable Click processInvalidSlot(ClientClickWindowPacket.ClickType type, byte button) {
        return switch (type) {
            case PICKUP, THROW -> {
                if (button == 0) yield new Click.LeftDropCursor();
                if (button == 1) yield new Click.RightDropCursor();
                yield null;
            }
            case CLONE -> {
                if (button == 2) yield new Click.MiddleDropCursor(); // Why Mojang, why?
                yield null;
            }
            case QUICK_CRAFT -> {
                // Trust me, a switch would not make this cleaner

                if (button == 2) {
                    var list = List.copyOf(leftDrag);
                    leftDrag.clear();
                    yield new Click.LeftDrag(list);
                } else if (button == 6) {
                    var list = List.copyOf(rightDrag);
                    rightDrag.clear();
                    yield new Click.RightDrag(list);
                } else if (button == 10) {
                    var list = List.copyOf(middleDrag);
                    middleDrag.clear();
                    yield new Click.MiddleDrag(list);
                }

                if (button == 0) leftDrag.clear();
                if (button == 4) rightDrag.clear();
                if (button == 8) middleDrag.clear();

                yield null;
            }
            default -> null;
        };
    }

    /**
     * Processes a click in a valid slot, possibly returning a result.
     */
    private @Nullable Click process(ClientClickWindowPacket.ClickType type, int slot, byte button) {
        return switch (type) {
            case PICKUP -> switch (button) {
                case 0 -> new Click.Left(slot);
                case 1 -> new Click.Right(slot);
                default -> null;
            };
            case QUICK_MOVE -> button == 0 ? new Click.LeftShift(slot) : new Click.RightShift(slot);
            case SWAP -> {
                if (button >= 0 && button < 9) {
                    yield new Click.HotbarSwap(button, slot);
                } else if (button == 40) {
                    yield new Click.OffhandSwap(slot);
                } else {
                    yield null;
                }
            }
            case CLONE -> new Click.Middle(slot);
            case THROW -> new Click.DropSlot(slot, button == 1);
            case QUICK_CRAFT -> {
                switch (button) {
                    case 1 -> leftDrag.add(slot);
                    case 5 -> rightDrag.add(slot);
                    case 9 -> middleDrag.add(slot);
                }
                yield null;
            }
            case PICKUP_ALL -> new Click.Double(slot);
        };
    }
}
