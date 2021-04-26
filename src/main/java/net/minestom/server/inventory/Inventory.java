package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link Inventory#Inventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(Inventory)}.
 */
public class Inventory extends AbstractInventory implements Viewable {

    // incremented each time an inventory is created (used in the window packets)
    private static byte LAST_INVENTORY_ID;

    // the id of this inventory
    private final byte id;
    // the type of this inventory
    private final InventoryType inventoryType;
    // the title of this inventory
    private Component title;

    private final int offset;

    // the players currently viewing this inventory
    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    // (player -> cursor item) map, used by the click listeners
    private final ConcurrentHashMap<Player, ItemStack> cursorPlayersItem = new ConcurrentHashMap<>();

    public Inventory(@NotNull InventoryType inventoryType, @NotNull Component title) {
        super(inventoryType.getSize());
        this.id = generateId();
        this.inventoryType = inventoryType;
        this.title = title;

        this.offset = getSize();
    }

    public Inventory(@NotNull InventoryType inventoryType, @NotNull String title) {
        this(inventoryType, Component.text(title));
    }

    private static synchronized byte generateId() {
        if (LAST_INVENTORY_ID == Byte.MAX_VALUE) {
            LAST_INVENTORY_ID = 0;
        }
        return ++LAST_INVENTORY_ID;
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
    public Component getTitle() {
        return title;
    }

    /**
     * Changes the inventory title.
     *
     * @param title the new inventory title
     */
    public void setTitle(@NotNull Component title) {
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
    public synchronized void clear() {
        super.clear();
        // Clear cursor
        getViewers().forEach(player ->
                setCursorItem(player, ItemStack.AIR));
    }

    /**
     * Refreshes the inventory for all viewers.
     */
    @Override
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
    @NotNull
    public ItemStack getCursorItem(@NotNull Player player) {
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
        final boolean similar = currentCursorItem.isSimilar(cursorItem);

        if (!similar) {
            final SetSlotPacket setSlotPacket = SetSlotPacket.createCursorPacket(cursorItem);
            player.getPlayerConnection().sendPacket(setSlotPacket);
        }

        if (!cursorItem.isAir()) {
            this.cursorPlayersItem.put(player, cursorItem);
        } else {
            this.cursorPlayersItem.remove(player);
        }
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
    @Override
    protected synchronized void safeItemInsert(int slot, @NotNull ItemStack itemStack) {
        this.itemStacks[slot] = itemStack;
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = getWindowId();
        setSlotPacket.slot = (short) slot;
        setSlotPacket.itemStack = itemStack;
        sendPacketToViewers(setSlotPacket);
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
    private void refreshPlayerCursorItem(@NotNull Player player, @NotNull ItemStack itemStack) {
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
        refreshPlayerCursorItem(player, clickResult.getCursor());

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
        refreshPlayerCursorItem(player, clickResult.getCursor());

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

        final InventoryClickResult clickResult = clickProcessor.shiftClick(
                isInWindow ? playerInventory : this,
                isInWindow ? this : null,
                player, slot, clicked, cursor);

        if (clickResult == null)
            return false;

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }

        if(clickResult.doRefresh()){
            update(player);
        }

        refreshPlayerCursorItem(player, clickResult.getCursor());

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
                ItemStack.AIR : (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot));
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

        refreshPlayerCursorItem(player, clickResult.getCursor());

        return !clickResult.isCancel();
    }

    @Override
    public boolean dragging(@NotNull Player player, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = slot != -999 ?
                (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot)) :
                ItemStack.AIR;
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

        refreshPlayerCursorItem(player, clickResult.getCursor());

        return !clickResult.isCancel();
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = getCursorItem(player);
        final boolean isInWindow = isClickInWindow(slot);

        final InventoryClickResult clickResult = clickProcessor.doubleClick(isInWindow ? this : playerInventory,
                this, player, slot, cursor);

        if (clickResult == null)
            return false;

        if (clickResult.doRefresh())
            updateFromClick(clickResult, player);

        refreshPlayerCursorItem(player, clickResult.getCursor());

        return !clickResult.isCancel();
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
