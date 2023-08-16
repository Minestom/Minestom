package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.IntIterators;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.inventory.click.*;
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
import org.jetbrains.annotations.Nullable;

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
public class AbstractInventory implements Taggable, Viewable {

    private static final VarHandle ITEM_UPDATER = MethodHandles.arrayElementVarHandle(ItemStack[].class);

    private final int size;
    protected final ItemStack[] itemStacks;

    // list of conditions/callbacks assigned to this inventory
    protected final List<InventoryCondition> inventoryConditions = new CopyOnWriteArrayList<>();

    protected final ClickPreprocessor clickPreprocessor = new ClickPreprocessor(this);

    private final TagHandler tagHandler = TagHandler.newHandler();

    // the players currently viewing this inventory
    protected final Set<Player> viewers = new CopyOnWriteArraySet<>();
    protected final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    // (player -> cursor item) map, used by the click listeners
    protected final Map<Player, ItemStack> cursorPlayersItem = new ConcurrentHashMap<>();

    public static final @NotNull ClickHandler DEFAULT_HANDLER = new StandardClickHandler(
            (player, inventory, item, slot) -> slot >= ClickPreprocessor.PLAYER_INVENTORY_OFFSET ?
                    IntIterators.fromTo(0, inventory.getSize()) :
                    PlayerInventory.getInnerShiftClickSlots(),
            (player, inventory, item, slot) -> IntIterators.concat(
                    IntIterators.fromTo(0, inventory.getSize()),
                    PlayerInventory.getInnerDoubleClickSlots()
            ));

    protected AbstractInventory(int size) {
        this.size = size;
        this.itemStacks = new ItemStack[getSize()];
        Arrays.fill(itemStacks, ItemStack.AIR);
    }

    // Basic getters and setters

    /**
     * Gets the size of the inventory.
     *
     * @return the inventory's size
     */
    public int getSize() {
        return size;
    }

    public byte getWindowId() {
        return 1;
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

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    /**
     * Gets the click preprocessor for this inventory.
     *
     * @return the click preprocessor
     */
    public @NotNull ClickPreprocessor getClickPreprocessor() {
        return clickPreprocessor;
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
        refreshCursor(player, cursorItem);
        if (!cursorItem.isAir()) {
            this.cursorPlayersItem.put(player, cursorItem);
        } else {
            this.cursorPlayersItem.remove(player);
        }
    }

    // Viewer management

    @Override
    public @NotNull Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    /**
     * This will not perform all of the operations required to open the inventory for the player - use
     * {@link Player#openInventory(AbstractInventory)}.
     *
     * @param player the viewer to add
     * @return true if the player has successfully been added
     */
    @Override
    public boolean addViewer(@NotNull Player player) {
        if (!this.viewers.add(player)) return false;

        handleOpen(player);
        return true;
    }

    /**
     * This will not perform all of the operations required to close the inventory for the player - use
     * {@link Player#closeInventory()}.
     *
     * @param player the viewer to remove
     * @return true if the player has successfully been removed
     */
    @Override
    public boolean removeViewer(@NotNull Player player) {
        if (!this.viewers.remove(player)) return false;

        handleClose(player);
        return true;
    }

    // Various updating and refreshing code

    /**
     * Handles when a player opens this inventory, without actually dealing with viewers.
     *
     * @param player the player opening this inventory
     */
    public void handleOpen(@NotNull Player player) {
        update(player);
    }

    /**
     * Handles when a player closes this inventory, without actually dealing with viewers.
     *
     * @param player the player closing this inventory
     */
    public void handleClose(@NotNull Player player) {
        ItemStack cursorItem = getCursorItem(player);

        if (!cursorItem.isAir()) {
            // Drop the item if it can not be added back to the inventory
            if (!player.getInventory().addItemStack(cursorItem)) {
                player.dropItem(cursorItem);
            }
        }

        setCursorItem(player, ItemStack.AIR);
        getClickPreprocessor().clearCache(player);
        player.sendPacket(new CloseWindowPacket(getWindowId()));
    }

    public void refreshSlot(int slot, @NotNull ItemStack itemStack) {
        sendPacketToViewers(new SetSlotPacket(getWindowId(), 0, (short) slot, itemStack));
    }

    public void refreshCursor(@NotNull Player player, @NotNull ItemStack cursorItem) {
        final ItemStack currentCursorItem = cursorPlayersItem.getOrDefault(player, ItemStack.AIR);
        if (!currentCursorItem.equals(cursorItem)) {
            player.sendPacket(SetSlotPacket.createCursorPacket(cursorItem));
        }
    }

    public void update() {
        this.viewers.forEach(this::update);
    }

    /**
     * Refreshes the inventory for a specific viewer. The player must be a viewer; make sure this is true.
     *
     * @param player the player to update the inventory
     */
    public void update(@NotNull Player player) {
        player.sendPacket(new WindowItemsPacket(getWindowId(), 0, List.of(itemStacks), cursorPlayersItem.getOrDefault(player, ItemStack.AIR)));
    }

    // Click handling

    /**
     * Handles the provided click from the given player, returning the results after it is applied. If the results are
     * null, this indicates that the click was cancelled or was otherwise not processed.
     *
     * @param player the player that clicked
     * @param clickInfo the information about the player's click
     * @return the results of the click, or null if the click was cancelled or otherwise was not handled
     */
    public @Nullable ClickResult handleClick(@NotNull Player player, @NotNull ClickInfo clickInfo) {
        return DEFAULT_HANDLER.handleClick(this, player, clickInfo);
    }

    // Item get methods

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

    // Inventory modification methods

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

    protected final void safeItemInsert(int slot, @NotNull ItemStack itemStack) {
        safeItemInsert(slot, itemStack, true);
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
            previous = itemStacks[slot];
            if (itemStack.equals(previous)) return; // Avoid sending updates if the item has not changed

            UNSAFE_itemInsert(slot, itemStack);
            if (sendPacket) refreshSlot(slot, itemStack);
        }

        EventDispatcher.call(new InventoryItemChangeEvent(this, slot, previous, itemStack));
    }

    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack) {
        itemStacks[slot] = itemStack;
    }

    public synchronized void replaceItemStack(int slot, @NotNull UnaryOperator<@NotNull ItemStack> operator) {
        var currentItem = getItemStack(slot);
        setItemStack(slot, operator.apply(currentItem));
    }

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

    public synchronized <T> @NotNull T processItemStack(@NotNull ItemStack itemStack,
                                                        @NotNull TransactionType type,
                                                        @NotNull TransactionOption<T> option) {
        return option.fill(type, this, itemStack);
    }

    public synchronized <T> @NotNull List<@NotNull T> processItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                                        @NotNull TransactionType type,
                                                                        @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(item -> result.add(processItemStack(item, type, option)));
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
        return processItemStack(itemStack, TransactionType.add(() -> IntIterators.fromTo(0, getSize()), () -> IntIterators.fromTo(0, getSize())), option);
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
    public synchronized <T> @NotNull List<@NotNull T> addItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                                    @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(item -> result.add(addItemStack(item, option)));
        return result;
    }

    /**
     * Takes an {@link ItemStack} from the inventory and sends relevant update to the viewer(s).
     *
     * @param itemStack the item to take
     * @return true if the item has been successfully fully taken, false otherwise
     */
    public <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.take(() -> IntIterators.fromTo(0, getSize())), option);
    }

    /**
     * Takes {@link ItemStack}s from the inventory and sends relevant updates to the viewer(s).
     *
     * @param itemStacks items to take
     * @return the operation results
     */
    public synchronized <T> @NotNull List<@NotNull T> takeItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                                     @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(item -> result.add(takeItemStack(item, option)));
        return result;
    }

}
