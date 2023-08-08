package net.minestom.server.inventory;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.inventory.click.InventoryClickProcessor;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
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
import java.util.concurrent.ConcurrentHashMap;
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
    // (player -> cursor item) map, used by the click listeners
    protected final ConcurrentHashMap<Player, ItemStack> cursorPlayersItem = new ConcurrentHashMap<>();

    protected AbstractInventory(int size) {
        this.size = size;
        this.itemStacks = new ItemStack[getSize()];
        Arrays.fill(itemStacks, ItemStack.AIR);
    }

    /**
     * Sets an {@link ItemStack} at the specified slot and send relevant update to the viewer(s).
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     */
    public synchronized void setItemStack(int slot, @NotNull ItemStack itemStack) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, getSize()),
                "Inventory does not have the slot " + slot);
        safeItemInsert(slot, itemStack);
    }

    /**
     * Inserts safely an item into the inventory.
     * <p>
     * This will update the slot for all viewers and warn the inventory that
     * the window items packet is not up-to-date.
     *
     * @param slot      the internal slot id
     * @param itemStack the item to insert (use air instead of null)
     * @throws IllegalArgumentException if the slot {@code slot} does not exist
     */
    protected final void safeItemInsert(int slot, @NotNull ItemStack itemStack, boolean sendPacket) {
        ItemStack previous;
        synchronized (this) {
            Check.argCondition(
                    !MathUtils.isBetween(slot, 0, getSize()),
                    "The slot {0} does not exist in this inventory",
                    slot
            );
            previous = itemStacks[slot];
            if (itemStack.equals(previous)) return; // Avoid sending updates if the item has not changed
            UNSAFE_itemInsert(slot, itemStack, sendPacket);
        }

        EventDispatcher.call(new InventoryItemChangeEvent(this, slot, previous, itemStack));
    }

    protected final void safeItemInsert(int slot, @NotNull ItemStack itemStack) {
        safeItemInsert(slot, itemStack, true);
    }

    protected abstract void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack, boolean sendPacket);

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

    public abstract byte getWindowId();

    /**
     * Clears the inventory and send relevant update to the viewer(s).
     */
    public synchronized void clear() {
        this.cursorPlayersItem.clear();

        // Clear the item array
        for (int i = 0; i < size; i++) {
            safeItemInsert(i, ItemStack.AIR, false);
        }
        // Send the cleared inventory to viewers
        update();
    }

    /**
     * Refreshes the inventory for a specific viewer.
     * <p>
     * The player needs to be a viewer, otherwise nothing is sent.
     *
     * @param player the player to update the inventory
     */
    public void update(@NotNull Player player) {
        if (!isViewer(player)) return;
        player.sendPacket(createNewWindowItemsPacket(player));
    }

    public void update() {
        this.viewers.forEach(this::update);
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    /**
     * This will not open the inventory for {@code player}, use {@link Player#openInventory(Inventory)}.
     *
     * @param player the viewer to add
     * @return true if the player has successfully been added
     */
    @Override
    public boolean addViewer(@NotNull Player player) {
        final boolean result = this.viewers.add(player);
        update(player);
        return result;
    }

    /**
     * This will not close the inventory for {@code player}, use {@link Player#closeInventory()}.
     *
     * @param player the viewer to remove
     * @return true if the player has successfully been removed
     */
    @Override
    public boolean removeViewer(@NotNull Player player) {
        final boolean result = this.viewers.remove(player);
        setCursorItem(player, ItemStack.AIR);
        this.clickProcessor.clearCache(player);
        return result;
    }

    /**
     * Gets the cursor item of a viewer.
     *
     * @param player the player to get the cursor item from
     * @return the player cursor item, air item if the player is not a viewer
     */
    public @NotNull ItemStack getCursorItem(@NotNull Player player) {
        return cursorPlayersItem.getOrDefault(player, ItemStack.AIR);
    }

    /**
     * Changes the cursor item of a viewer,
     * does nothing if <code>player</code> is not a viewer.
     *
     * @param player     the player to change the cursor item
     * @param cursorItem the new player cursor item
     */
    public void setCursorItem(@NotNull Player player, @NotNull ItemStack cursorItem) {
        final ItemStack currentCursorItem = cursorPlayersItem.getOrDefault(player, ItemStack.AIR);
        if (!currentCursorItem.equals(cursorItem)) {
            player.sendPacket(SetSlotPacket.createCursorPacket(cursorItem));
        }
        if (!cursorItem.isAir()) {
            this.cursorPlayersItem.put(player, cursorItem);
        } else {
            this.cursorPlayersItem.remove(player);
        }
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

    private @NotNull WindowItemsPacket createNewWindowItemsPacket(Player player) {
        return new WindowItemsPacket(getWindowId(), 0, List.of(getItemStacks()), cursorPlayersItem.getOrDefault(player, ItemStack.AIR));
    }
}
