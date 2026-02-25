package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

import java.util.List;

/**
 * Represents an inventory which can receive click input.
 * All methods returning boolean returns true if the action is successful, false otherwise.
 * <p>
 * See <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Click_Container">the Minecraft wiki</a> for more information.
 */
public sealed interface InventoryClickHandler permits AbstractInventory {

    /**
     * Parses a click. This delegates to each individual implementation method.
     * @param player the player who clicked
     * @param click the click that occurred
     * @return whether or not the click was a success
     */
    default boolean handleClick(Player player, Click click) {
        // Maps a click back into the click handler interface.
        // This is so that we can maintain normal
        return switch (click) {
            case Click.Left(int slot) -> leftClick(player, slot);
            case Click.Right(int slot) -> rightClick(player, slot);
            case Click.Middle(int slot) -> middleClick(player, slot);

            case Click.LeftShift(int slot) -> shiftClick(player, slot, 0);
            case Click.RightShift(int slot) -> shiftClick(player, slot, 1);

            case Click.Double(int slot) -> doubleClick(player, slot);

            case Click.LeftDrag(List<Integer> slots) -> dragging(player, slots, 2);
            case Click.RightDrag(List<Integer> slots) -> dragging(player, slots,  6);
            case Click.MiddleDrag(List<Integer> slots) -> dragging(player, slots, 10);

            case Click.LeftDropCursor() -> drop(player, true, -999);
            case Click.RightDropCursor() -> drop(player, false, -999);
            case Click.MiddleDropCursor() -> false; // Does nothing currently

            case Click.DropSlot(int slot, boolean all) -> drop(player, all, slot);

            case Click.HotbarSwap(int hotbarSlot, int slot) -> changeHeld(player, slot, hotbarSlot);

            case Click.OffhandSwap(int slot) -> changeHeld(player, slot, PlayerInventoryUtils.OFFHAND_SLOT);
        };
    }

    /**
     * Called when a {@link Player} left click in the inventory. Can also be to drop the cursor item
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean leftClick(Player player, int slot);

    /**
     * Called when a {@link Player} right click in the inventory. Can also be to drop the cursor item
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean rightClick(Player player, int slot);

    /**
     * Called when a {@link Player} shift click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @param button the button (same behaviour in vanilla, but can be used for custom behaviour)
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean shiftClick(Player player, int slot, int button);

    /**
     * Called when a {@link Player} held click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @param key    the held slot (0-8) pressed
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean changeHeld(Player player, int slot, int key);

    boolean middleClick(Player player, int slot);

    /**
     * Called when a {@link Player} press the drop button
     *
     * @param player the player who clicked
     * @param all
     * @param slot   the slot number (-999 if clicking outside, i.e. dropping cursor)
     * @return true if the drop hasn't been cancelled, false otherwise
     */
    boolean drop(Player player, boolean all, int slot);

    boolean dragging(Player player, List<Integer> slots, int button);

    /**
     * Called when a {@link Player} double click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean doubleClick(Player player, int slot);

    default void callClickEvent(Player player, AbstractInventory inventory, int slot,
                                ClickType clickType, ItemStack clicked, ItemStack cursor) {
        EventDispatcher.call(new InventoryClickEvent(inventory, player, slot, clickType, clicked, cursor));
    }
}
