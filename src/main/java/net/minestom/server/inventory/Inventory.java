package net.minestom.server.inventory;

import net.minestom.server.Viewable;
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
import net.minestom.server.utils.item.ItemStackUtils;
import net.minestom.server.utils.validate.Check;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class Inventory implements InventoryModifier, InventoryClickHandler, Viewable {

    private static AtomicInteger lastInventoryId = new AtomicInteger();

    private final byte id;
    private final InventoryType inventoryType;
    private String title;

    private final int size;

    private final int offset;

    private final ItemStack[] itemStacks;
    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final ConcurrentHashMap<Player, ItemStack> cursorPlayersItem = new ConcurrentHashMap<>();

    private final List<InventoryCondition> inventoryConditions = new CopyOnWriteArrayList<>();
    private final InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    // Cached windows packet

    public Inventory(InventoryType inventoryType, String title) {
        this.id = generateId();
        this.inventoryType = inventoryType;
        this.title = title;

        this.size = inventoryType.getAdditionalSlot();

        this.offset = size;

        this.itemStacks = new ItemStack[size];

        ArrayUtils.fill(itemStacks, ItemStack::getAirItem);
    }

    private static byte generateId() {
        byte newInventoryId = (byte) lastInventoryId.incrementAndGet();
        if (newInventoryId == Byte.MAX_VALUE)
            newInventoryId = 1;
        return newInventoryId;
    }

    /**
     * Get the inventory type
     *
     * @return the inventory type
     */
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    /**
     * Get the inventory title
     *
     * @return the inventory title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Change the inventory title
     *
     * @param title the new inventory title
     */
    public void setTitle(String title) {
        this.title = title;

        OpenWindowPacket packet = new OpenWindowPacket();

        packet.windowId = getWindowId();
        packet.windowType = getInventoryType().getWindowType();
        packet.title = title;

        // Re-open the inventory
        sendPacketToViewers(packet);
        // Send inventory items
        update();
    }

    /**
     * Get this window id
     * <p>
     * This is the id that the client will send to identify the affected inventory, mostly used by packets
     *
     * @return the window id
     */
    public byte getWindowId() {
        return id;
    }

    @Override
    public void setItemStack(int slot, ItemStack itemStack) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, getSize()),
                inventoryType.toString() + " does not have slot " + slot);

        safeItemInsert(slot, itemStack);
    }

    @Override
    public synchronized boolean addItemStack(ItemStack itemStack) {
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
        // TODO cached packet with empty content
        update();
    }

    @Override
    public ItemStack getItemStack(int slot) {
        return itemStacks[slot];
    }

    @Override
    public ItemStack[] getItemStacks() {
        return Arrays.copyOf(itemStacks, itemStacks.length);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public List<InventoryCondition> getInventoryConditions() {
        return inventoryConditions;
    }

    @Override
    public void addInventoryCondition(InventoryCondition inventoryCondition) {
        this.inventoryConditions.add(inventoryCondition);
    }

    /**
     * Refresh the inventory for all viewers
     */
    public void update() {
        sendPacketToViewers(createNewWindowItemsPacket());
    }

    /**
     * Refresh the inventory for a specific viewer
     * the player needs to be a viewer, otherwise nothing is sent
     *
     * @param player the player to update the inventory
     */
    public void update(Player player) {
        if (!getViewers().contains(player))
            return;

        final PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(createNewWindowItemsPacket());
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    @Override
    public boolean addViewer(Player player) {
        final boolean result = this.viewers.add(player);
        update(player);
        return result;
    }

    @Override
    public boolean removeViewer(Player player) {
        final boolean result = this.viewers.remove(player);
        this.cursorPlayersItem.remove(player);
        this.clickProcessor.clearCache(player);
        return result;
    }

    /**
     * Get the cursor item of a viewer
     *
     * @param player the player to get the cursor item from
     * @return the player cursor item, air item if the player is not a viewer
     */
    public ItemStack getCursorItem(Player player) {
        return cursorPlayersItem.getOrDefault(player, ItemStack.getAirItem());
    }

    /**
     * Change the cursor item of a viewer,
     * does nothing if <code>player</code> is not a viewer
     *
     * @param player     the player to change the cursor item
     * @param cursorItem the new player cursor item
     */
    public void setCursorItem(Player player, ItemStack cursorItem) {
        if (!isViewer(player))
            return;

        cursorItem = ItemStackUtils.notNull(cursorItem);

        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = -1;
        setSlotPacket.slot = -1;
        setSlotPacket.itemStack = cursorItem;
        player.getPlayerConnection().sendPacket(setSlotPacket);

        this.cursorPlayersItem.put(player, cursorItem);
    }

    /**
     * Insert safely an item into the inventory
     * <p>
     * This will update the slot for all viewers and warn the inventory that
     * the window items packet is not up-to-date
     *
     * @param slot      the internal slot id
     * @param itemStack the item to insert
     */
    private synchronized void safeItemInsert(int slot, ItemStack itemStack) {
        itemStack = ItemStackUtils.notNull(itemStack);
        setItemStackInternal(slot, itemStack);
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = getWindowId();
        setSlotPacket.slot = (short) slot;
        setSlotPacket.itemStack = itemStack;
        sendPacketToViewers(setSlotPacket);
    }

    /**
     * Insert an item into the inventory without notifying viewers
     * <p>
     * This will also warn the inventory that the cached window items packet is
     * not up-to-date
     *
     * @param slot      the internal slot
     * @param itemStack the item to insert
     */
    protected void setItemStackInternal(int slot, ItemStack itemStack) {
        itemStacks[slot] = itemStack;
    }

    /**
     * Create a complete new {@link WindowItemsPacket}
     *
     * @return a new {@link WindowItemsPacket} packet
     */
    private WindowItemsPacket createNewWindowItemsPacket() {
        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = getWindowId();
        windowItemsPacket.items = getItemStacks();
        return windowItemsPacket;
    }

    /**
     * Send a window property to all viewers
     *
     * @param property the property to send
     * @param value    the value of the property
     * @see <a href="https://wiki.vg/Protocol#Window_Property">https://wiki.vg/Protocol#Window_Property</a>
     */
    protected void sendProperty(InventoryProperty property, short value) {
        WindowPropertyPacket windowPropertyPacket = new WindowPropertyPacket();
        windowPropertyPacket.windowId = getWindowId();
        windowPropertyPacket.property = property.getProperty();
        windowPropertyPacket.value = value;
        sendPacketToViewers(windowPropertyPacket);
    }

    /**
     * Change the internal player's cursor item
     * <p>
     * WARNING: the player will not be notified by the change
     *
     * @param player    the player to change the cursor item
     * @param itemStack the cursor item
     */
    private void setCursorPlayerItem(Player player, ItemStack itemStack) {
        this.cursorPlayersItem.put(player, itemStack);
    }

    private boolean isClickInWindow(int slot) {
        return slot < getSize();
    }

    @Override
    public boolean leftClick(Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = getCursorItem(player);
        final boolean isInWindow = isClickInWindow(slot);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);


        final InventoryClickResult clickResult = clickProcessor.leftClick(this, player, slot, clicked, cursor);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        } else {
            playerInventory.setItemStack(slot, offset, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        }

        if (!clickResult.isCancel())
            callClickEvent(player, this, slot, ClickType.LEFT_CLICK, clicked, cursor);

        return !clickResult.isCancel();
    }

    @Override
    public boolean rightClick(Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = getCursorItem(player);
        final boolean isInWindow = isClickInWindow(slot);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        final InventoryClickResult clickResult = clickProcessor.rightClick(this, player, slot, clicked, cursor);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        } else {
            playerInventory.setItemStack(slot, offset, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        }

        if (!clickResult.isCancel())
            callClickEvent(player, this, slot, ClickType.RIGHT_CLICK, clicked, cursor);

        return !clickResult.isCancel();
    }

    @Override
    public boolean shiftClick(Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);
        final ItemStack cursor = getCursorItem(player); // Isn't used in the algorithm


        final InventoryClickResult clickResult;

        if (isInWindow) {
            clickResult = clickProcessor.shiftClick(this, player, slot, clicked, cursor,
                    // Player inventory loop
                    new InventoryClickLoopHandler(0, PlayerInventory.INVENTORY_SIZE, 1,
                            i -> PlayerInventoryUtils.convertToPacketSlot(i),
                            index -> isClickInWindow(index) ? getItemStack(index) : playerInventory.getItemStack(index, offset),
                            (index, itemStack) -> {
                                if (isClickInWindow(index)) {
                                    setItemStack(index, itemStack);
                                } else {
                                    playerInventory.setItemStack(index, offset, itemStack);
                                }
                            }));
        } else {
            clickResult = clickProcessor.shiftClick(this, player, slot, clicked, cursor,
                    // Window loop
                    new InventoryClickLoopHandler(0, getSize(), 1,
                            i -> i,
                            index -> isClickInWindow(index) ? getItemStack(index) : playerInventory.getItemStack(index, offset),
                            (index, itemStack) -> {
                                if (isClickInWindow(index)) {
                                    setItemStack(index, itemStack);
                                } else {
                                    playerInventory.setItemStack(index, offset, itemStack);
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
    public boolean changeHeld(Player player, int slot, int key) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);
        final ItemStack heldItem = playerInventory.getItemStack(key);

        final InventoryClickResult clickResult = clickProcessor.changeHeld(this, player, slot, key, clicked, heldItem);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(slot, offset, clickResult.getClicked());
        }
        playerInventory.setItemStack(key, clickResult.getCursor());

        if (!clickResult.isCancel())
            callClickEvent(player, this, slot, ClickType.CHANGE_HELD, clicked, getCursorItem(player));

        // Weird synchronization issue when omitted
        updateFromClick(clickResult, player);

        return !clickResult.isCancel();
    }

    @Override
    public boolean middleClick(Player player, int slot) {
        // TODO
        return false;
    }

    @Override
    public boolean drop(Player player, int mode, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final ItemStack clicked = slot == -999 ?
                null : (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset));
        final ItemStack cursor = getCursorItem(player);

        final InventoryClickResult clickResult = clickProcessor.drop(this, player,
                mode, slot, button, clicked, cursor);

        if (clickResult.doRefresh()) {
            updateFromClick(clickResult, player);
        }

        ItemStack resultClicked = clickResult.getClicked();
        if (isInWindow) {
            if (resultClicked != null)
                setItemStack(slot, resultClicked);
            setCursorPlayerItem(player, clickResult.getCursor());
        } else {
            if (resultClicked != null)
                playerInventory.setItemStack(slot, offset, resultClicked);
            setCursorPlayerItem(player, clickResult.getCursor());
        }

        return !clickResult.isCancel();
    }

    @Override
    public boolean dragging(Player player, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = null;
        final ItemStack cursor = getCursorItem(player);
        if (slot != -999)
            clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        final InventoryClickResult clickResult = clickProcessor.dragging(this, player,
                slot, button,
                clicked, cursor,

                s -> isClickInWindow(s) ? getItemStack(s) : playerInventory.getItemStack(s, offset),

                (s, item) -> {
                    if (isClickInWindow(s)) {
                        setItemStack(s, item);
                    } else {
                        playerInventory.setItemStack(s, offset, item);
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
    public boolean doubleClick(Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = getCursorItem(player);


        final InventoryClickResult clickResult = clickProcessor.doubleClick(this, player, slot, cursor,
                // Start by looping through the opened inventory
                new InventoryClickLoopHandler(0, getSize(), 1,
                        i -> i,
                        index -> getItemStack(index),
                        (index, itemStack) -> setItemStack(index, itemStack)),
                // Looping through player inventory
                new InventoryClickLoopHandler(0, PlayerInventory.INVENTORY_SIZE - 9, 1,
                        i -> PlayerInventoryUtils.convertToPacketSlot(i),
                        index -> playerInventory.getItemStack(index, offset),
                        (index, itemStack) -> playerInventory.setItemStack(index, offset, itemStack)),
                // Player hotbar
                new InventoryClickLoopHandler(0, 9, 1,
                        i -> PlayerInventoryUtils.convertToPacketSlot(i),
                        index -> playerInventory.getItemStack(index, offset),
                        (index, itemStack) -> {
                            playerInventory.setItemStack(index, offset, itemStack);
                        }));

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
}
