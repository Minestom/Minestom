package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickPreprocessor;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Represents a generic inventory that can be interacted with.
 */
public sealed interface Inventory extends Taggable, Viewable permits InventoryImpl {

    interface Typed {

        /**
         * Gets the inventory type of this inventory.
         *
         * @return the inventory type
         */
        @NotNull InventoryType getInventoryType();

        /**
         * Gets the inventory title of this inventory.
         *
         * @return the inventory title
         */
        @NotNull Component getTitle();

        /**
         * Changes the inventory title of this inventory.
         *
         * @param title the new inventory title
         */
        void setTitle(@NotNull Component title);

    }

    /**
     * Gets the size of this inventory. This should be a constant number.
     * @return the size
     */
    int getSize();

    /**
     * Gets the {@link ItemStack} at the specified slot.
     *
     * @param slot the slot to check
     * @return the item in the slot {@code slot}
     */
    @NotNull ItemStack getItemStack(int slot);

    /**
     * Sets an {@link ItemStack} at the specified slot and send relevant update to the viewer(s).
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     */
    void setItemStack(int slot, @NotNull ItemStack itemStack);

    /**
     * Gets the window ID of this window, as a byte.
     *
     * @return the window ID
     */
    byte getWindowId();

    /**
     * Gets the cursor item of a viewer.
     *
     * @param player the player to get the cursor item from
     * @return the player cursor item, air item if the player is not a viewer
     */
    @NotNull ItemStack getCursorItem(@NotNull Player player);

    /**
     * Changes the cursor item of a viewer,
     * does nothing if <code>player</code> is not a viewer.
     *
     * @param player     the player to change the cursor item
     * @param cursorItem the new player cursor item
     */
    void setCursorItem(@NotNull Player player, @NotNull ItemStack cursorItem);

    /**
     * Handles the provided click from the given player, returning the results after it is applied. If the results are
     * null, this indicates that the click was cancelled or was otherwise not processed.
     *
     * @param player the player that clicked
     * @param clickInfo the information about the player's click
     * @return the results of the click, or null if the click was cancelled or otherwise was not handled
     */
    @Nullable ClickResult handleClick(@NotNull Player player, @NotNull ClickInfo clickInfo);

    /**
     * Gets the click preprocessor for this inventory.
     *
     * @return the click preprocessor
     */
    @NotNull ClickPreprocessor getClickPreprocessor();

    /**
     * Gets all the {@link InventoryCondition} of this inventory.
     *
     * @return a modifiable {@link List} containing all the inventory conditions
     */
    @NotNull List<@NotNull InventoryCondition> getInventoryConditions();

    /**
     * Adds a new {@link InventoryCondition} to this inventory.
     *
     * @param inventoryCondition the inventory condition to add
     */
    void addInventoryCondition(@NotNull InventoryCondition inventoryCondition);

    /**
     * Gets all the {@link ItemStack} in the inventory.
     * <p>
     * Be aware that the returned array does not need to be the original one,
     * meaning that modifying it directly may not work.
     *
     * @return an array containing all the inventory's items
     */
    @NotNull ItemStack[] getItemStacks();

    /**
     * Places all the items of {@code itemStacks} into the internal array.
     *
     * @param itemStacks the array to copy the content from
     * @throws IllegalArgumentException if the size of the array is not equal to {@link #getSize()}
     * @throws NullPointerException     if {@code itemStacks} contains one null element or more
     */
    void copyContents(@NotNull ItemStack[] itemStacks);

    /**
     * Clears the inventory and send relevant update to the viewer(s).
     */
    void clear();

    /**
     * Handles when a player opens this inventory, without actually updating viewers.
     *
     * @param player the player opening this inventory
     */
    void handleOpen(@NotNull Player player);

    /**
     * Handles when a player closes this inventory, without actually updating viewers.
     *
     * @param player the player closing this inventory
     */
    void handleClose(@NotNull Player player);

    /**
     * Updates the provided slot for this inventory's viewers.
     *
     * @param slot the slot to update
     * @param itemStack the item treated as in the slot
     */
    void updateSlot(int slot, @NotNull ItemStack itemStack);

    /**
     * Updates the cursor item for the provided player.
     *
     * @param player the player to update
     * @param cursorItem the cursor item to send to the player
     */
    void updateCursor(@NotNull Player player, @NotNull ItemStack cursorItem);

    /**
     * Updates the inventory for all viewers.
     */
    void update();

    /**
     * Updates the inventory for a specific viewer.
     *
     * @param player the player to update the inventory for
     */
    void update(@NotNull Player player);

    /**
     * Replaces the item in the slot according to the operator.
     *
     * @param slot the slot to replace
     * @param operator the operator to apply to the slot
     */
    void replaceItemStack(int slot, @NotNull UnaryOperator<@NotNull ItemStack> operator);

    <T> @NotNull T processItemStack(@NotNull ItemStack itemStack,
                                                        @NotNull TransactionType type, @NotNull TransactionOption<T> option);

    <T> @NotNull List<@NotNull T> processItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                    @NotNull TransactionType type, @NotNull TransactionOption<T> option);

    /**
     * Adds an {@link ItemStack} to the inventory and sends relevant update to the viewer(s).
     *
     * @param itemStack the item to add
     * @param option    the transaction option
     * @return true if the item has been successfully added, false otherwise
     */
    <T> @NotNull T addItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option);

    boolean addItemStack(@NotNull ItemStack itemStack);

    /**
     * Adds {@link ItemStack}s to the inventory and sends relevant updates to the viewer(s).
     *
     * @param itemStacks items to add
     * @param option     the transaction option
     * @return the operation results
     */
    <T> @NotNull List<@NotNull T> addItemStacks(@NotNull List<@NotNull ItemStack> itemStacks, @NotNull TransactionOption<T> option);

    /**
     * Takes an {@link ItemStack} from the inventory and sends relevant update to the viewer(s).
     *
     * @param itemStack the item to take
     * @return true if the item has been successfully fully taken, false otherwise
     */
    <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option);

    /**
     * Takes {@link ItemStack}s from the inventory and sends relevant updates to the viewer(s).
     *
     * @param itemStacks items to take
     * @return the operation results
     */
    <T> @NotNull List<@NotNull T> takeItemStacks(@NotNull List<@NotNull ItemStack> itemStacks, @NotNull TransactionOption<T> option);

}
