package net.minestom.server.inventory;

import net.minestom.server.Viewable;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickLoopHandler;
import net.minestom.server.inventory.click.InventoryClickProcessor;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link Inventory#Inventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(Inventory)}.
 */
public class Inventory implements InventoryModifier, InventoryClickHandler, Viewable, DataContainer {

    // incremented each time an inventory is created (used in the window packets)
    private static final AtomicInteger LAST_INVENTORY_ID = new AtomicInteger();

    // the id of this inventory
    private final byte id;
    // the type of this inventory
    private final InventoryType inventoryType;
    // the title of this inventory)
    private String title;

    // the size based on the inventory type
    private final int size;

    private final int offset;

    // the items in this inventory
    private final ItemStack[] itemStacks;
    // the players currently viewing this inventory
    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    // (player -> cursor item) map, used by the click listeners
    private final ConcurrentHashMap<Player, ItemStack> cursorPlayersItem = new ConcurrentHashMap<>();

    // list of conditions/callbacks assigned to this inventory
    private final List<InventoryCondition> inventoryConditions = new CopyOnWriteArrayList<>();
    // the click processor which process all the clicks in the inventory
    private final InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    private Data data;

    public Inventory(@NotNull InventoryType inventoryType, @NotNull String title) {
        this.id = generateId();
        this.inventoryType = inventoryType;
        this.title = title;

        this.size = inventoryType.getSize();

        this.offset = size;

        this.itemStacks = new ItemStack[size];

        ArrayUtils.fill(itemStacks, ItemStack::getAirItem);
    }

    private static byte generateId() {
        byte newInventoryId = (byte) LAST_INVENTORY_ID.incrementAndGet();
        if (newInventoryId == Byte.MAX_VALUE)
            newInventoryId = 1;
        return newInventoryId;
    }

    /**
     * Gets the inventory type.
     *
     * @return the inventory type
     */
    @NotNull
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    /**
     * Gets the inventory title.
     *
     * @return the inventory title
     */
    @NotNull
    public String getTitle() {
        return title;
    }

    /**
     * Changes the inventory title.
     *
     * @param title the new inventory title
     */
    public void setTitle(@NotNull String title) {
        this.title = title;

        OpenWindowPacket packet = new OpenWindowPacket(title);

        packet.windowId = getWindowId();
        packet.windowType = getInventoryType().getWindowType();

        // Re-open the inventory
        sendPacketToViewers(packet);
        // Send inventory items
        update();
    }

    /**
     * Gets this window id.
     * <p>
     * This is the id that the client will send to identify the affected inventory, mostly used by packets.
     *
     * @return the window id
     */
    public byte getWindowId() {
        return id;
    }

    @Override
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, getSize()),
                inventoryType.toString() + " does not have slot " + slot);

        safeItemInsert(slot, itemStack);
    }

    @Override
    public synchronized boolean addItemStack(@NotNull ItemStack itemStack) {
        final StackingRule stackingRule = itemStack.getStackingRule();
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItemStack(i);
            final StackingRule itemStackingRule = item.getStackingRule();
            if (itemStackingRule.canBeStacked(itemStack, item)) {
                final int itemAmount = itemStackingRule.getAmount(item);
                if (itemAmount == stackingRule.getMaxSize())
                    continue;
                final int itemStackAmount = itemStackingRule.getAmount(itemStack);
                final int totalAmount = itemStackAmount + itemAmount;
                if (!stackingRule.canApply(itemStack, totalAmount)) {
                    item = itemStackingRule.apply(item, itemStackingRule.getMaxSize());

                    sendSlotRefresh((short) i, item);
                    itemStack = stackingRule.apply(itemStack, totalAmount - stackingRule.getMaxSize());
                } else {
                    item.setAmount((byte) totalAmount);
                    sendSlotRefresh((short) i, item);
                    return true;
                }
            } else if (item.isAir()) {
                setItemStack(i, itemStack);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        // Clear the item array
        for (int i = 0; i < getSize(); i++) {
            setItemStackInternal(i, ItemStack.getAirItem());
        }
        // Send the cleared inventory to viewers
        update();
    }


    @NotNull
    @Override
    public ItemStack getItemStack(int slot) {
        return itemStacks[slot];
    }

    @NotNull
    @Override
    public ItemStack[] getItemStacks() {
        return itemStacks.clone();
    }

    @Override
    public int getSize() {
        return size;
    }

    @NotNull
    @Override
    public List<InventoryCondition> getInventoryConditions() {
        return inventoryConditions;
    }

    @Override
    public void addInventoryCondition(@NotNull InventoryCondition inventoryCondition) {
        this.inventoryConditions.add(inventoryCondition);
    }

    /**
     * Refreshes the inventory for all viewers.
     */
    public void update() {
        sendPacketToViewers(createNewWindowItemsPacket());
    }

    /**
     * Refreshes the inventory for a specific viewer.
     * <p>
     * The player needs to be a viewer, otherwise nothing is sent.
     *
     * @param player the player to update the inventory
     */
    public void update(@NotNull Player player) {
        if (!getViewers().contains(player))
            return;

        final PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(createNewWindowItemsPacket());
    }

    /**
     * Refreshes only a specific slot with the updated item stack data.
     *
     * @param slot the slot to refresh
     */
    public void refreshSlot(short slot) {
        sendSlotRefresh(slot, getItemStack(slot));
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
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
        this.cursorPlayersItem.remove(player);
        this.clickProcessor.clearCache(player);
        return result;
    }

    /**
     * Gets the cursor item of a viewer.
     *
     * @param player the player to get the cursor item from
     * @return the player cursor item, air item if the player is not a viewer
     */
    @NotNull
    public ItemStack getCursorItem(@NotNull Player player) {
        return cursorPlayersItem.getOrDefault(player, ItemStack.getAirItem());
    }

    /**
     * Changes the cursor item of a viewer,
     * does nothing if <code>player</code> is not a viewer.
     *
     * @param player     the player to change the cursor item
     * @param cursorItem the new player cursor item
     */
    public void setCursorItem(@NotNull Player player, @NotNull ItemStack cursorItem) {
        if (!isViewer(player))
            return;

        final ItemStack currentCursorItem = cursorPlayersItem.get(player);
        final boolean similar = currentCursorItem != null && currentCursorItem.isSimilar(cursorItem);

        if (!similar) {
            final SetSlotPacket setSlotPacket = SetSlotPacket.createCursorPacket(cursorItem);
            player.getPlayerConnection().sendPacket(setSlotPacket);
        }

        this.cursorPlayersItem.put(player, cursorItem);
    }

    /**
     * Inserts safely an item into the inventory.
     * <p>
     * This will update the slot for all viewers and warn the inventory that
     * the window items packet is not up-to-date.
     *
     * @param slot      the internal slot id
     * @param itemStack the item to insert
     */
    private synchronized void safeItemInsert(int slot, @NotNull ItemStack itemStack) {
        setItemStackInternal(slot, itemStack);
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = getWindowId();
        setSlotPacket.slot = (short) slot;
        setSlotPacket.itemStack = itemStack;
        sendPacketToViewers(setSlotPacket);
    }

    /**
     * Inserts an item into the inventory without notifying viewers.
     * <p>
     * This will also warn the inventory that the cached window items packet is
     * not up-to-date.
     *
     * @param slot      the internal slot
     * @param itemStack the item to insert
     */
    protected void setItemStackInternal(int slot, @NotNull ItemStack itemStack) {
        itemStacks[slot] = itemStack;
    }

    /**
     * Creates a complete new {@link WindowItemsPacket}.
     *
     * @return a new {@link WindowItemsPacket} packet
     */
    @NotNull
    private WindowItemsPacket createNewWindowItemsPacket() {
        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = getWindowId();
        windowItemsPacket.items = getItemStacks();
        return windowItemsPacket;
    }

    /**
     * Sends a window property to all viewers.
     *
     * @param property the property to send
     * @param value    the value of the property
     * @see <a href="https://wiki.vg/Protocol#Window_Property">https://wiki.vg/Protocol#Window_Property</a>
     */
    protected void sendProperty(@NotNull InventoryProperty property, short value) {
        WindowPropertyPacket windowPropertyPacket = new WindowPropertyPacket();
        windowPropertyPacket.windowId = getWindowId();
        windowPropertyPacket.property = property.getProperty();
        windowPropertyPacket.value = value;
        sendPacketToViewers(windowPropertyPacket);
    }

    /**
     * Changes the internal player's cursor item
     * <p>
     * WARNING: the player will not be notified by the change
     *
     * @param player    the player to change the cursor item
     * @param itemStack the cursor item
     */
    private void setCursorPlayerItem(@NotNull Player player, @NotNull ItemStack itemStack) {
        this.cursorPlayersItem.put(player, itemStack);
    }

    private boolean isClickInWindow(int slot) {
        return slot < getSize();
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = getCursorItem(player);
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);

        final InventoryClickResult clickResult = clickProcessor.leftClick(isInWindow ? this : null, player, slot, clicked, cursor);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }
        setCursorPlayerItem(player, clickResult.getCursor());

        if (!clickResult.isCancel())
            callClickEvent(player, isInWindow ? this : null, slot, ClickType.LEFT_CLICK, clicked, cursor);

        return !clickResult.isCancel();
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = getCursorItem(player);
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);

        final InventoryClickResult clickResult = clickProcessor.rightClick(isInWindow ? this : null, player, slot, clicked, cursor);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }
        setCursorPlayerItem(player, clickResult.getCursor());

        if (!clickResult.isCancel())
            callClickEvent(player, isInWindow ? this : null, slot, ClickType.RIGHT_CLICK, clicked, cursor);

        return !clickResult.isCancel();
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final ItemStack cursor = getCursorItem(player); // Isn't used in the algorithm


        final InventoryClickResult clickResult;

        if (isInWindow) {
            clickResult = clickProcessor.shiftClick(this, player, slot, clicked, cursor,
                    // Player inventory loop
                    new InventoryClickLoopHandler(0, PlayerInventory.INVENTORY_SIZE, 1,
                            PlayerInventoryUtils::convertToPacketSlot,
                            index -> isClickInWindow(index) ?
                                    getItemStack(index) :
                                    playerInventory.getItemStack(PlayerInventoryUtils.convertSlot(index, offset)),
                            (index, itemStack) -> {
                                if (isClickInWindow(index)) {
                                    setItemStack(index, itemStack);
                                } else {
                                    playerInventory.setItemStack(PlayerInventoryUtils.convertSlot(index, offset), itemStack);
                                }
                            }));
        } else {
            clickResult = clickProcessor.shiftClick(null, player, slot, clicked, cursor,
                    // Window loop
                    new InventoryClickLoopHandler(0, getSize(), 1,
                            i -> i,
                            index -> isClickInWindow(index) ?
                                    getItemStack(index) :
                                    playerInventory.getItemStack(PlayerInventoryUtils.convertSlot(index, offset)),
                            (index, itemStack) -> {
                                if (isClickInWindow(index)) {
                                    setItemStack(index, itemStack);
                                } else {
                                    playerInventory.setItemStack(PlayerInventoryUtils.convertSlot(index, offset), itemStack);
                                }
                            }));
        }

        if (clickResult == null)
            return false;

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        setCursorPlayerItem(player, clickResult.getCursor());
        playerInventory.update();
        update();

        return !clickResult.isCancel();
    }

    @Override
    public boolean changeHeld(@NotNull Player player, int slot, int key) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final ItemStack heldItem = playerInventory.getItemStack(key);

        final InventoryClickResult clickResult = clickProcessor.changeHeld(isInWindow ? this : null, player, slot, key, clicked, heldItem);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }
        playerInventory.setItemStack(key, clickResult.getCursor());

        if (!clickResult.isCancel())
            callClickEvent(player, isInWindow ? this : null, slot, ClickType.CHANGE_HELD, clicked, getCursorItem(player));

        // Weird synchronization issue when omitted
        updateFromClick(clickResult, player);

        return !clickResult.isCancel();
    }

    @Override
    public boolean middleClick(@NotNull Player player, int slot) {
        // TODO
        return false;
    }

    @Override
    public boolean drop(@NotNull Player player, int mode, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final boolean outsideDrop = slot == -999;
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = outsideDrop ?
                ItemStack.getAirItem() : (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot));
        final ItemStack cursor = getCursorItem(player);

        final InventoryClickResult clickResult = clickProcessor.drop(isInWindow ? this : null, player,
                mode, slot, button, clicked, cursor);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        final ItemStack resultClicked = clickResult.getClicked();
        if (!outsideDrop && resultClicked != null) {
            if (isInWindow) {
                setItemStack(slot, resultClicked);
            } else {
                playerInventory.setItemStack(clickSlot, resultClicked);
            }
        }

        setCursorPlayerItem(player, clickResult.getCursor());

        return !clickResult.isCancel();
    }

    @Override
    public boolean dragging(@NotNull Player player, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = slot != -999 ?
                (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot)) :
                ItemStack.getAirItem();
        final ItemStack cursor = getCursorItem(player);

        final InventoryClickResult clickResult = clickProcessor.dragging(isInWindow ? this : null, player,
                slot, button,
                clicked, cursor,

                s -> isClickInWindow(s) ? getItemStack(s) :
                        playerInventory.getItemStack(PlayerInventoryUtils.convertSlot(s, offset)),

                (s, item) -> {
                    if (isClickInWindow(s)) {
                        setItemStack(s, item);
                    } else {
                        playerInventory.setItemStack(PlayerInventoryUtils.convertSlot(s, offset), item);
                    }
                });

        if (clickResult == null) {
            return false;
        }

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        setCursorPlayerItem(player, clickResult.getCursor());

        return !clickResult.isCancel();
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = getCursorItem(player);
        final boolean isInWindow = isClickInWindow(slot);

        final InventoryClickResult clickResult = clickProcessor.doubleClick(isInWindow ? this : null, player, slot, cursor,
                // Start by looping through the opened inventory
                new InventoryClickLoopHandler(0, getSize(), 1,
                        i -> i,
                        this::getItemStack,
                        this::setItemStack),
                // Looping through player inventory
                new InventoryClickLoopHandler(0, PlayerInventory.INVENTORY_SIZE, 1,
                        PlayerInventoryUtils::convertToPacketSlot,
                        index -> playerInventory.getItemStack(index, PlayerInventoryUtils.OFFSET),
                        (index, itemStack) -> playerInventory.setItemStack(index, PlayerInventoryUtils.OFFSET, itemStack)));

        if (clickResult == null)
            return false;

        if (clickResult.doRefresh())
            updateFromClick(clickResult, player);

        setCursorPlayerItem(player, clickResult.getCursor());

        return !clickResult.isCancel();
    }

    /**
     * Refresh a slot for all viewers
     * <p>
     * WARNING: this does not update the items in the inventory, this is only visual
     *
     * @param slot      the packet slot
     * @param itemStack the item stack to set at the slot
     */
    private void sendSlotRefresh(short slot, ItemStack itemStack) {
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = getWindowId();
        setSlotPacket.slot = slot;
        setSlotPacket.itemStack = itemStack;
        sendPacketToViewers(setSlotPacket);
    }

    /**
     * Used to update the inventory for a specific player in order to fix his cancelled actions
     *
     * @param clickResult the action result
     * @param player      the player who did the action
     */
    private void updateFromClick(InventoryClickResult clickResult, Player player) {
        if (clickResult.isPlayerInventory()) {
            player.getInventory().update();
        } else {
            update(player);
        }
    }

    @Nullable
    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(@Nullable Data data) {
        this.data = data;
    }
}
