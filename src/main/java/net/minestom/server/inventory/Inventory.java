package net.minestom.server.inventory;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Represents a generic inventory that can be interacted with.
 */
public sealed interface Inventory extends Taggable, Viewable permits InventoryImpl {

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
     * Handles the provided click from the given player, returning the results after it is applied. If the results are
     * null, this indicates that the click was cancelled or was otherwise not processed.
     *
     * @param player the player that clicked
     * @param info the information about the player's click
     * @return the results of the click, or null if the click was cancelled or otherwise was not handled
     */
    @Nullable Click.Result handleClick(@NotNull Player player, @NotNull Click.Info info);

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
                                    @NotNull TransactionType type,
                                    @NotNull TransactionOption<T> option);

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
