package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.IntIterators;
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

sealed class InventoryImpl implements Inventory permits ContainerInventory, PlayerInventory {

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
            (builder, item, slot) -> slot >= builder.clickedInventory().getSize() ?
                    IntIterators.fromTo(0, builder.clickedInventory().getSize()) :
                    PlayerInventory.getInnerShiftClickSlots(builder, item, slot),
            (builder, item, slot) -> IntIterators.concat(
                    IntIterators.fromTo(0, builder.clickedInventory().getSize()),
                    PlayerInventory.getInnerDoubleClickSlots(builder, item, slot)
            ));

    protected InventoryImpl(int size) {
        this.size = size;
        this.itemStacks = new ItemStack[getSize()];
        Arrays.fill(itemStacks, ItemStack.AIR);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public byte getWindowId() {
        return 1;
    }

    @Override
    public @NotNull List<@NotNull InventoryCondition> getInventoryConditions() {
        return inventoryConditions;
    }

    @Override
    public void addInventoryCondition(@NotNull InventoryCondition inventoryCondition) {
        this.inventoryConditions.add(inventoryCondition);
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    @Override
    public @NotNull ClickPreprocessor getClickPreprocessor() {
        return clickPreprocessor;
    }

    @Override
    public @NotNull ItemStack getCursorItem(@NotNull Player player) {
        return cursorPlayersItem.getOrDefault(player, ItemStack.AIR);
    }

    @Override
    public void setCursorItem(@NotNull Player player, @NotNull ItemStack cursorItem) {
        refreshCursor(player, cursorItem);
        if (!cursorItem.isAir()) {
            this.cursorPlayersItem.put(player, cursorItem);
        } else {
            this.cursorPlayersItem.remove(player);
        }
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        if (!this.viewers.add(player)) return false;

        handleOpen(player);
        return true;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        if (!this.viewers.remove(player)) return false;

        handleClose(player);
        return true;
    }

    @Override
    public void handleOpen(@NotNull Player player) {
        update(player);
    }

    @Override
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
        if (player.didCloseInventory()) {
            player.UNSAFE_changeDidCloseInventory(false);
        } else {
            player.sendPacket(new CloseWindowPacket(getWindowId()));
        }
    }

    @Override
    public void refreshSlot(int slot, @NotNull ItemStack itemStack) {
        sendPacketToViewers(new SetSlotPacket(getWindowId(), 0, (short) slot, itemStack));
    }

    @Override
    public void refreshCursor(@NotNull Player player, @NotNull ItemStack cursorItem) {
        final ItemStack currentCursorItem = cursorPlayersItem.getOrDefault(player, ItemStack.AIR);
        if (!currentCursorItem.equals(cursorItem)) {
            player.sendPacket(SetSlotPacket.createCursorPacket(cursorItem));
        }
    }

    @Override
    public void update() {
        this.viewers.forEach(this::update);
    }

    @Override
    public void update(@NotNull Player player) {
        player.sendPacket(new WindowItemsPacket(getWindowId(), 0, List.of(itemStacks), cursorPlayersItem.getOrDefault(player, ItemStack.AIR)));
    }

    @Override
    public @Nullable ClickResult handleClick(@NotNull Player player, @NotNull ClickInfo clickInfo) {
        return DEFAULT_HANDLER.handleClick(this, player, clickInfo);
    }

    @Override
    public @NotNull ItemStack getItemStack(int slot) {
        return (ItemStack) ITEM_UPDATER.getVolatile(itemStacks, slot);
    }

    @Override
    public @NotNull ItemStack[] getItemStacks() {
        return itemStacks.clone();
    }

    @Override
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

    @Override
    public synchronized void replaceItemStack(int slot, @NotNull UnaryOperator<@NotNull ItemStack> operator) {
        var currentItem = getItemStack(slot);
        setItemStack(slot, operator.apply(currentItem));
    }

    @Override
    public synchronized void clear() {
        this.cursorPlayersItem.clear();

        // Clear the item array
        for (int i = 0; i < size; i++) {
            safeItemInsert(i, ItemStack.AIR, false);
        }
        // Send the cleared inventory to viewers
        update();
    }

    @Override
    public synchronized <T> @NotNull T processItemStack(@NotNull ItemStack itemStack,
                                                        @NotNull TransactionType type,
                                                        @NotNull TransactionOption<T> option) {
        return option.fill(type, this, itemStack);
    }

    @Override
    public synchronized <T> @NotNull List<@NotNull T> processItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                                        @NotNull TransactionType type,
                                                                        @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(item -> result.add(processItemStack(item, type, option)));
        return result;
    }

    @Override
    public <T> @NotNull T addItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.add(() -> IntIterators.fromTo(0, getSize()), () -> IntIterators.fromTo(0, getSize())), option);
    }

    @Override
    public boolean addItemStack(@NotNull ItemStack itemStack) {
        return addItemStack(itemStack, TransactionOption.ALL_OR_NOTHING);
    }

    @Override
    public synchronized <T> @NotNull List<@NotNull T> addItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                                    @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(item -> result.add(addItemStack(item, option)));
        return result;
    }

    @Override
    public <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.take(() -> IntIterators.fromTo(0, getSize())), option);
    }

    @Override
    public synchronized <T> @NotNull List<@NotNull T> takeItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                                     @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(item -> result.add(takeItemStack(item, option)));
        return result;
    }

}
