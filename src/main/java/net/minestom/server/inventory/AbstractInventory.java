package net.minestom.server.inventory;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.inventory.click.InventoryClickProcessor;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.CloseWindowPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.UnaryOperator;

/**
 * Represents an inventory where items can be modified/retrieved.
 */
public sealed abstract class AbstractInventory implements InventoryClickHandler, Taggable, Viewable
        permits Inventory, PlayerInventory {

    private static final VarHandle ITEM_UPDATER = MethodHandles.arrayElementVarHandle(ItemStack[].class);

    private final int size;
    protected final ItemStack[] itemStacks;

    // list of conditions/callbacks assigned to this inventory
    protected final List<InventoryCondition> inventoryConditions = new CopyOnWriteArrayList<>();
    // the click processor which process all the clicks in the inventory
    protected final InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    private final TagHandler tagHandler = TagHandler.newHandler();

    // the players currently viewing this inventory
    protected final Set<Player> viewers = new CopyOnWriteArraySet<>();
    protected final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);

    protected AbstractInventory(int size) {
        this.size = size;
        this.itemStacks = new ItemStack[getSize()];
        Arrays.fill(itemStacks, ItemStack.AIR);
    }

    /**
     * Gets this window id.
     * <p>
     * This is the id that the client will send to identify the affected inventory, mostly used by packets.
     *
     * @return the window id
     */
    public abstract byte getWindowId();

    @Override
    public @NotNull Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        if (!this.viewers.add(player)) return false;

        update(player);
        return true;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        if (!this.viewers.remove(player)) return false;

        // Drop cursor item when closing inventory
        ItemStack cursorItem = player.getInventory().getCursorItem();
        player.getInventory().setCursorItem(ItemStack.AIR);

        if (!cursorItem.isAir()) {
            if (!player.dropItem(cursorItem)) {
                player.getInventory().addItemStack(cursorItem);
            }
        }

        if (player.didCloseInventory()) {
            player.sendPacket(new CloseWindowPacket(getWindowId()));
        }

        return true;
    }

    /**
     * Sets an {@link ItemStack} at the specified slot and send relevant update to the viewer(s).
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     */
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
        setItemStack(slot, itemStack, true);
    }

    /**
     * Sets an {@link ItemStack} at the specified slot and send relevant update to the viewer(s).
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     * @param sendPacket whether or not to send packets
     */
    public void setItemStack(int slot, @NotNull ItemStack itemStack, boolean sendPacket) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, getSize()),
                "Inventory does not have the slot " + slot);

        ItemStack previous;
        synchronized (this) {
            previous = itemStacks[slot];
            if (itemStack.equals(previous)) return; // Avoid sending updates if the item has not changed
            UNSAFE_itemInsert(slot, itemStack, previous, sendPacket);
        }
        EventDispatcher.call(new InventoryItemChangeEvent(this, slot, previous, itemStack));
    }

    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack item, @NotNull ItemStack previous, boolean sendPacket) {
        itemStacks[slot] = item;
        if (sendPacket) sendSlotRefresh(slot, item, previous);
    }

    public void sendSlotRefresh(int slot, @NotNull ItemStack item, @NotNull ItemStack previous) {
        sendPacketToViewers(new SetSlotPacket(getWindowId(), 0, (short) slot, item));
    }

    public synchronized <T> @NotNull T processItemStack(@NotNull ItemStack itemStack,
                                                        @NotNull TransactionType type,
                                                        @NotNull TransactionOption<T> option) {
        return option.fill(type, this, itemStack);
    }

    public synchronized <T> @NotNull List<@NotNull T> processItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                                        @NotNull TransactionType type,
                                                                        @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(itemStack -> {
            T transactionResult = processItemStack(itemStack, type, option);
            result.add(transactionResult);
        });
        return result;
    }

    /**
     * Adds an {@link ItemStack} to the inventory and sends relevant update to the viewer(s).
     *
     * @param itemStack the item to add
     * @param option    the transaction option
     * @return true if the item has been successfully added, false otherwise
     */
    public <T> @NotNull T addItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.ADD, option);
    }

    public boolean addItemStack(@NotNull ItemStack itemStack) {
        return addItemStack(itemStack, TransactionOption.ALL_OR_NOTHING);
    }

    /**
     * Adds {@link ItemStack}s to the inventory and sends relevant updates to the viewer(s).
     *
     * @param itemStacks items to add
     * @param option     the transaction option
     * @return the operation results
     */
    public <T> @NotNull List<@NotNull T> addItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                       @NotNull TransactionOption<T> option) {
        return processItemStacks(itemStacks, TransactionType.ADD, option);
    }

    /**
     * Takes an {@link ItemStack} from the inventory and sends relevant update to the viewer(s).
     *
     * @param itemStack the item to take
     * @return true if the item has been successfully fully taken, false otherwise
     */
    public <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.TAKE, option);
    }

    /**
     * Takes {@link ItemStack}s from the inventory and sends relevant updates to the viewer(s).
     *
     * @param itemStacks items to take
     * @return the operation results
     */
    public <T> @NotNull List<@NotNull T> takeItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                        @NotNull TransactionOption<T> option) {
        return processItemStacks(itemStacks, TransactionType.TAKE, option);
    }

    public synchronized void replaceItemStack(int slot, @NotNull UnaryOperator<@NotNull ItemStack> operator) {
        var currentItem = getItemStack(slot);
        setItemStack(slot, operator.apply(currentItem));
    }

    /**
     * Clears the inventory and send relevant update to the viewer(s).
     */
    public synchronized void clear() {
        // Clear the item array
        for (int i = 0; i < size; i++) {
            setItemStack(i, ItemStack.AIR, false);
        }
        // Send the cleared inventory to viewers
        update();
    }

    /**
     * Refreshes the inventory for all viewers.
     */
    public void update() {
        this.viewers.forEach(this::update);
    }

    /**
     * Refreshes the inventory for a specific viewer.
     *
     * @param player the player to update the inventory for
     */
    public void update(@NotNull Player player) {
        player.sendPacket(new WindowItemsPacket(getWindowId(), 0, List.of(itemStacks), player.getInventory().getCursorItem()));
    }

    /**
     * Gets the {@link ItemStack} at the specified slot.
     *
     * @param slot the slot to check
     * @return the item in the slot {@code slot}
     */
    public @NotNull ItemStack getItemStack(int slot) {
        return (ItemStack) ITEM_UPDATER.getVolatile(itemStacks, slot);
    }

    /**
     * Gets all the {@link ItemStack} in the inventory.
     * <p>
     * Be aware that the returned array does not need to be the original one,
     * meaning that modifying it directly may not work.
     *
     * @return an array containing all the inventory's items
     */
    public @NotNull ItemStack[] getItemStacks() {
        return itemStacks.clone();
    }

    /**
     * Gets the size of the inventory.
     *
     * @return the inventory's size
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the size of the "inner inventory" (which includes only "usable" slots).
     *
     * @return inner inventory's size
     */
    public int getInnerSize() {
        return getSize();
    }

    /**
     * Gets all the {@link InventoryCondition} of this inventory.
     *
     * @return a modifiable {@link List} containing all the inventory conditions
     */
    public @NotNull List<@NotNull InventoryCondition> getInventoryConditions() {
        return inventoryConditions;
    }

    /**
     * Adds a new {@link InventoryCondition} to this inventory.
     *
     * @param inventoryCondition the inventory condition to add
     */
    public void addInventoryCondition(@NotNull InventoryCondition inventoryCondition) {
        this.inventoryConditions.add(inventoryCondition);
    }

    /**
     * Places all the items of {@code itemStacks} into the internal array.
     *
     * @param itemStacks the array to copy the content from
     * @throws IllegalArgumentException if the size of the array is not equal to {@link #getSize()}
     * @throws NullPointerException     if {@code itemStacks} contains one null element or more
     */
    public void copyContents(@NotNull ItemStack[] itemStacks) {
        Check.argCondition(itemStacks.length != getSize(),
                "The size of the array has to be of the same size as the inventory: " + getSize());

        for (int i = 0; i < itemStacks.length; i++) {
            final ItemStack itemStack = itemStacks[i];
            Check.notNull(itemStack, "The item array cannot contain any null element!");
            setItemStack(i, itemStack);
        }
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }
}
