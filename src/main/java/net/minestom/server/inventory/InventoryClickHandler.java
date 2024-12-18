package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an inventory which can receive click input.
 * All methods returning boolean returns true if the action is successful, false otherwise.
 * <p>
 * See <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Click_Container">the Minecraft wiki</a> for more information.
 */
public sealed interface InventoryClickHandler permits AbstractInventory {

    /**
     * Parses click info. This delegates to each individual implementation method.
     * @param player the player who clicked
     * @param info the info about the click
     * @return whether or not the click was a success
     */
    default boolean handleClick(@NotNull Player player, @NotNull Click.Info info) {
        // Maps a click back into the click handler interface.
        // This is so that we can maintain normal
        return switch (info) {
            case Click.Info.Left(int slot) -> leftClick(player, slot);
            case Click.Info.Right(int slot) -> rightClick(player, slot);
            case Click.Info.Middle(int slot) -> middleClick(player, slot);

            case Click.Info.LeftShift(int slot) -> shiftClick(player, slot, 0);
            case Click.Info.RightShift(int slot) -> shiftClick(player, slot, 1);

            case Click.Info.Double(int slot) -> doubleClick(player, slot);

            case Click.Info.LeftDrag(List<Integer> slots) -> dragging(player, slots, 2);
            case Click.Info.RightDrag(List<Integer> slots) -> dragging(player, slots,  6);
            case Click.Info.MiddleDrag(List<Integer> slots) -> dragging(player, slots, 10);

            case Click.Info.LeftDropCursor() -> drop(player, true, -999);
            case Click.Info.RightDropCursor() -> drop(player, false, -999);
            case Click.Info.MiddleDropCursor() -> false; // Does nothing currently

            case Click.Info.DropSlot(int slot, boolean all) -> drop(player, all, slot);

            case Click.Info.HotbarSwap(int hotbarSlot, int clickedSlot) -> changeHeld(player, clickedSlot, hotbarSlot);

            case Click.Info.OffhandSwap(int slot) -> changeHeld(player, slot, PlayerInventoryUtils.OFFHAND_SLOT);
        };
    }

    /**
     * Called when a {@link Player} left click in the inventory. Can also be to drop the cursor item
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean leftClick(@NotNull Player player, int slot);

    /**
     * Called when a {@link Player} right click in the inventory. Can also be to drop the cursor item
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean rightClick(@NotNull Player player, int slot);

    /**
     * Called when a {@link Player} shift click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @param button the button (same behaviour in vanilla, but can be used for custom behaviour)
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean shiftClick(@NotNull Player player, int slot, int button);

    /**
     * Called when a {@link Player} held click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @param key    the held slot (0-8) pressed
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean changeHeld(@NotNull Player player, int slot, int key);

    boolean middleClick(@NotNull Player player, int slot);

    /**
     * Called when a {@link Player} press the drop button
     *
     * @param player the player who clicked
     * @param all
     * @param slot   the slot number (-999 if clicking outside, i.e. dropping cursor)
     * @return true if the drop hasn't been cancelled, false otherwise
     */
    boolean drop(@NotNull Player player, boolean all, int slot);

    boolean dragging(@NotNull Player player, List<Integer> slots, int button);

    /**
     * Called when a {@link Player} double click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean doubleClick(@NotNull Player player, int slot);

    default void callClickEvent(@NotNull Player player, @NotNull AbstractInventory inventory, int slot,
                                @NotNull ClickType clickType, @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        EventDispatcher.call(new InventoryClickEvent(inventory, player, slot, clickType, clicked, cursor));
    }
}
