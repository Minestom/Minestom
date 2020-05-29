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
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
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

public class Inventory implements InventoryModifier, InventoryClickHandler, Viewable {

    private static volatile byte lastInventoryId;

    private byte id;
    private InventoryType inventoryType;
    private String title;

    private int size;

    private int offset;

    private ItemStack[] itemStacks;
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<Player, ItemStack> cursorPlayersItem = new ConcurrentHashMap<>();

    private List<InventoryCondition> inventoryConditions = new CopyOnWriteArrayList<>();
    private InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    public Inventory(InventoryType inventoryType, String title) {
        this.id = generateId();
        this.inventoryType = inventoryType;
        this.title = title;

        this.size = inventoryType.getAdditionalSlot();

        this.offset = size;

        this.itemStacks = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            itemStacks[i] = ItemStack.getAirItem();
        }
    }

    private static byte generateId() {
        byte newInventoryId = ++lastInventoryId;
        if (newInventoryId < 0)
            newInventoryId = 1;
        return newInventoryId;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public String getTitle() {
        return title;
    }

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
        StackingRule stackingRule = itemStack.getStackingRule();
        for (int i = 0; i < getItemStacks().length; i++) {
            ItemStack item = getItemStacks()[i];
            StackingRule itemStackingRule = item.getStackingRule();
            if (itemStackingRule.canBeStacked(itemStack, item)) {
                int itemAmount = itemStackingRule.getAmount(item);
                if (itemAmount == stackingRule.getMaxSize())
                    continue;
                int itemStackAmount = itemStackingRule.getAmount(itemStack);
                int totalAmount = itemStackAmount + itemAmount;
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
        PacketWriterUtils.writeAndSend(getViewers(), createWindowItemsPacket());
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

        PacketWriterUtils.writeAndSend(player, createWindowItemsPacket());
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    @Override
    public boolean addViewer(Player player) {
        boolean result = this.viewers.add(player);
        PacketWriterUtils.writeAndSend(player, createWindowItemsPacket());
        return result;
    }

    @Override
    public boolean removeViewer(Player player) {
        boolean result = this.viewers.remove(player);
        this.cursorPlayersItem.remove(player);
        this.clickProcessor.clearCache(player);
        return result;
    }

    public ItemStack getCursorItem(Player player) {
        return cursorPlayersItem.getOrDefault(player, ItemStack.getAirItem());
    }

    private synchronized void safeItemInsert(int slot, ItemStack itemStack) {
        itemStack = ItemStackUtils.notNull(itemStack);
        this.itemStacks[slot] = itemStack;
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = getWindowId();
        setSlotPacket.slot = (short) slot;
        setSlotPacket.itemStack = itemStack;
        sendPacketToViewers(setSlotPacket);
    }

    private WindowItemsPacket createWindowItemsPacket() {
        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = getWindowId();
        windowItemsPacket.count = (short) itemStacks.length;
        windowItemsPacket.items = itemStacks;
        return windowItemsPacket;
    }

    protected void sendProperty(InventoryProperty property, short value) {
        WindowPropertyPacket windowPropertyPacket = new WindowPropertyPacket();
        windowPropertyPacket.windowId = getWindowId();
        windowPropertyPacket.property = property.getProperty();
        windowPropertyPacket.value = value;
        sendPacketToViewers(windowPropertyPacket);
    }

    private void setCursorPlayerItem(Player player, ItemStack itemStack) {
        this.cursorPlayersItem.put(player, itemStack);
    }

    private boolean isClickInWindow(int slot) {
        return slot < getSize();
    }

    @Override
    public boolean leftClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursor = getCursorItem(player);
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);


        InventoryClickResult clickResult = clickProcessor.leftClick(this, player, slot, clicked, cursor);

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
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursor = getCursorItem(player);
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        InventoryClickResult clickResult = clickProcessor.rightClick(this, player, slot, clicked, cursor);

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
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);
        ItemStack cursor = getCursorItem(player); // Isn't used in the algorithm


        InventoryClickResult clickResult;

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
                    new InventoryClickLoopHandler(0, itemStacks.length, 1,
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
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);
        ItemStack heldItem = playerInventory.getItemStack(key);

        InventoryClickResult clickResult = clickProcessor.changeHeld(this, player, slot, key, clicked, heldItem);

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
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = slot == -999 ?
                null : (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset));
        ItemStack cursor = getCursorItem(player);

        InventoryClickResult clickResult = clickProcessor.drop(this, player,
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
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = null;
        ItemStack cursor = getCursorItem(player);
        if (slot != -999)
            clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        InventoryClickResult clickResult = clickProcessor.dragging(this, player,
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
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursor = getCursorItem(player);


        InventoryClickResult clickResult = clickProcessor.doubleClick(this, player, slot, cursor,
                // Start by looping through the opened inventory
                new InventoryClickLoopHandler(0, itemStacks.length, 1,
                        i -> i,
                        index -> itemStacks[index],
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
