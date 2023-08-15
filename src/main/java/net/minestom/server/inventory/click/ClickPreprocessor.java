package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Preprocesses click packets for an inventory, turning them into {@link ClickInfo} instances for further processing.
 */
public class ClickPreprocessor {

    /**
     * Player inventory slots start at this value when the player inventory is not the main one.
     */
    public static final int PLAYER_INVENTORY_OFFSET = 1000;

    private final @NotNull AbstractInventory inventory;

    private final Map<Player, IntSet> leftDraggingMap = new ConcurrentHashMap<>();
    private final Map<Player, IntSet> rightDraggingMap = new ConcurrentHashMap<>();
    private final Map<Player, IntSet> creativeDragMap = new ConcurrentHashMap<>();

    public ClickPreprocessor(@NotNull AbstractInventory inventory) {
        this.inventory = inventory;
    }

    public void clearCache(@NotNull Player player) {
        leftDraggingMap.remove(player);
        rightDraggingMap.remove(player);
        creativeDragMap.remove(player);
    }

    private static boolean validateSlot(@NotNull AbstractInventory inventory, int slot) {
        return slot >= 0 && slot < inventory.getSize() + (inventory.getWindowId() != 0 ? PlayerInventory.INNER_SIZE : 0);
    }

    private static int convertSlot(@NotNull AbstractInventory inventory, int slot) {
        if (slot < inventory.getSize()) {
            return slot;
        } else {
            return (slot - inventory.getSize() + 9) + PLAYER_INVENTORY_OFFSET; // Convert it to a player inventory slot
        }
    }

    /**
     * Processes the provided click packet, turning it into a {@link ClickInfo}.
     * @param player the player clicking
     * @param packet the raw click packet
     * @return the information about the click, or nothing if there was no immediately usable information
     */
    public @Nullable ClickInfo process(@NotNull Player player, @NotNull ClientClickWindowPacket packet) {
        final short slot = packet.slot();
        final byte button = packet.button();
        final ClientClickWindowPacket.ClickType clickType = packet.clickType();

        return switch (clickType) {
            case PICKUP -> {
                if (button == 0) {
                    if (slot == -999) {
                        yield new ClickInfo.DropCursor(true);
                    } else if (validateSlot(inventory, slot)) {
                        yield new ClickInfo.LeftClick(convertSlot(inventory, slot));
                    }
                } else if (button == 1) {
                    if (slot == -999) {
                        yield new ClickInfo.DropCursor(false);
                    } else if (validateSlot(inventory, slot)) {
                        yield new ClickInfo.RightClick(convertSlot(inventory, slot));
                    }
                }
                yield null;
            }
            case QUICK_MOVE -> validateSlot(inventory, slot) ? new ClickInfo.ShiftClick(convertSlot(inventory, slot)) : null;
            case SWAP -> {
                if (!validateSlot(inventory, slot)) {
                    yield null;
                } else if (button >= 0 && button < 9) {
                    yield new ClickInfo.HotbarSwap(button, convertSlot(inventory, slot));
                } else if (button == 40) {
                    yield new ClickInfo.OffhandSwap(convertSlot(inventory, slot));
                } else {
                    yield null;
                }
            }
            case CLONE -> (player.isCreative() && validateSlot(inventory, slot)) ? new ClickInfo.CopyItem(convertSlot(inventory, slot)) : null;
            case THROW -> validateSlot(inventory, slot) ? new ClickInfo.DropSlot(convertSlot(inventory, slot), button == 1) : null;
            case QUICK_CRAFT -> {
                // Prevent invalid creative actions
                if (!player.isCreative() && (button == 8 || button == 9 || button == 10)) yield null;

                // Handle drag finishes
                if (button == 2) {
                    var set = leftDraggingMap.remove(player);
                    yield new ClickInfo.DistributeCursor(set != null ? set : IntSets.emptySet(), true);
                } else if (button == 6) {
                    var set = rightDraggingMap.remove(player);
                    yield new ClickInfo.DistributeCursor(set != null ? set : IntSets.emptySet(), false);
                } else if (button == 10) {
                    var set = creativeDragMap.remove(player);
                    yield new ClickInfo.CopyCursor(set != null ? set : IntSets.emptySet());
                }

                // Handle intermediate state
                BiFunction<Player, IntSet, IntSet> addItem = (k, v) -> {
                    var v2 = v != null ? v : new IntArraySet();
                    if (validateSlot(inventory, slot)) {
                        v2.add(convertSlot(inventory, slot));
                    }
                    return v2;
                };

                switch (button) {
                    case 0 -> leftDraggingMap.remove(player);
                    case 4 -> rightDraggingMap.remove(player);
                    case 8 -> creativeDragMap.remove(player);

                    case 1 -> leftDraggingMap.compute(player, addItem);
                    case 5 -> rightDraggingMap.compute(player, addItem);
                    case 9 -> creativeDragMap.compute(player, addItem);
                }

                yield null;
            }
            case PICKUP_ALL -> validateSlot(inventory, slot) ? new ClickInfo.DoubleClick(convertSlot(inventory, slot)) : null;
        };
    }

}
