package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickProcessors;
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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

sealed abstract class InventoryImpl implements Inventory permits ContainerInventory, PlayerInventory {

    private static final VarHandle ITEM_UPDATER = MethodHandles.arrayElementVarHandle(ItemStack[].class);

    private final int size;
    protected final ItemStack[] itemStacks;

    private final TagHandler tagHandler = TagHandler.newHandler();

    protected final ReentrantLock lock = new ReentrantLock();

    // the players currently viewing this inventory
    protected final Set<Player> viewers = new CopyOnWriteArraySet<>();
    protected final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);

    protected InventoryImpl(int size) {
        this.size = size;
        this.itemStacks = new ItemStack[size];
        Arrays.fill(itemStacks, ItemStack.AIR);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

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

        ItemStack cursorItem = player.getInventory().getCursorItem();
        player.getInventory().setCursorItem(ItemStack.AIR);

        if (!cursorItem.isAir()) {
            // Drop the item if it can not be added back to the inventory
            if (!player.getInventory().addItemStack(cursorItem)) {
                player.dropItem(cursorItem);
            }
        }

        player.clickPreprocessor().clearCache();
        if (player.didCloseInventory()) {
            player.UNSAFE_changeDidCloseInventory(false);
        } else {
            player.sendPacket(new CloseWindowPacket(getWindowId()));
        }
        return true;
    }

    /**
     * Updates the provided slot for this inventory's viewers.
     *
     * @param slot      the slot to update
     * @param itemStack the item treated as in the slot
     */
    protected void updateSlot(int slot, @NotNull ItemStack itemStack) {
        sendPacketToViewers(new SetSlotPacket(getWindowId(), 0, (short) slot, itemStack));
    }

    @Override
    public void update() {
        this.viewers.forEach(this::update);
    }

    @Override
    public void update(@NotNull Player player) {
        player.sendPacket(new WindowItemsPacket(getWindowId(), 0, List.of(itemStacks), player.getInventory().getCursorItem()));
    }

    @Override
    public @Nullable Click.Result handleClick(@NotNull Player player, @NotNull Click.Info info) {
        var processor = ClickProcessors.standard(
                (builder, item, slot) -> slot >= getSize() ?
                        IntStream.range(0, getSize()) :
                        PlayerInventory.getInnerShiftClickSlots(getSize()),
                (builder, item, slot) -> IntStream.concat(
                        IntStream.range(0, getSize()),
                        PlayerInventory.getInnerDoubleClickSlots(getSize())
                ));
        return ContainerInventory.handleClick(this, player, info, processor);
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
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
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
        lock.lock();

        try {
            ItemStack previous = itemStacks[slot];
            if (itemStack.equals(previous)) return; // Avoid sending updates if the item has not changed

            UNSAFE_itemInsert(slot, itemStack);
            if (sendPacket) updateSlot(slot, itemStack);

            EventDispatcher.call(new InventoryItemChangeEvent(this, slot, previous, itemStack));
        } finally {
            lock.unlock();
        }
    }

    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack) {
        itemStacks[slot] = itemStack;
    }

    @Override
    public void replaceItemStack(int slot, @NotNull UnaryOperator<@NotNull ItemStack> operator) {
        lock.lock();

        try {
            var currentItem = getItemStack(slot);
            setItemStack(slot, operator.apply(currentItem));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();

        try {
            for (Player viewer : getViewers()) {
                viewer.getInventory().setCursorItem(ItemStack.AIR, false);
            }

            // Clear the item array
            for (int i = 0; i < size; i++) {
                safeItemInsert(i, ItemStack.AIR, false);
            }
            // Send the cleared inventory to viewers
            update();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> @NotNull T processItemStack(@NotNull ItemStack itemStack,
                                           @NotNull TransactionType type,
                                           @NotNull TransactionOption<T> option) {
        lock.lock();
        try {
            return option.fill(type, this, itemStack);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> @NotNull List<@NotNull T> processItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                           @NotNull TransactionType type,
                                                           @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());

        lock.lock();
        try {
            for (ItemStack item : itemStacks) {
                result.add(processItemStack(item, type, option));
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public <T> @NotNull T addItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        List<Integer> slots = IntStream.range(0, getSize()).boxed().toList();
        return processItemStack(itemStack, TransactionType.add(slots, slots), option);
    }

    @Override
    public boolean addItemStack(@NotNull ItemStack itemStack) {
        return addItemStack(itemStack, TransactionOption.ALL_OR_NOTHING);
    }

    @Override
    public <T> @NotNull List<@NotNull T> addItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                       @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());

        lock.lock();
        try {
            for (ItemStack item : itemStacks) {
                result.add(addItemStack(item, option));
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.take(IntStream.range(0, getSize()).boxed().toList()), option);
    }

    @Override
    public <T> @NotNull List<@NotNull T> takeItemStacks(@NotNull List<@NotNull ItemStack> itemStacks,
                                                        @NotNull TransactionOption<T> option) {
        List<T> result = new ArrayList<>(itemStacks.size());

        lock.lock();
        try {
            for (ItemStack item : itemStacks) {
                result.add(takeItemStack(item, option));
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

}
