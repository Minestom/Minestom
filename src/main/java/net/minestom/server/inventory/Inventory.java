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
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link Inventory#Inventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(Inventory)}.
 */
public non-sealed class Inventory extends AbstractInventory implements Viewable {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

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

    private static byte generateId() {
        return (byte) ID_COUNTER.updateAndGet(i -> i + 1 >= 128 ? 1 : i + 1);
    }

    /**
     * Gets the inventory type.
     *
     * @return the inventory type
     */
    public @NotNull InventoryType getInventoryType() {
        return inventoryType;
    }

    /**
     * Gets the inventory title.
     *
     * @return the inventory title
     */
    public @NotNull Component getTitle() {
        return title;
    }

    /**
     * Changes the inventory title.
     *
     * @param title the new inventory title
     */
    public void setTitle(@NotNull Component title) {
        this.title = title;
        // Re-open the inventory
        sendPacketToViewers(new OpenWindowPacket(getWindowId(), getInventoryType().getWindowType(), title));
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

    /**
     * Refreshes the inventory for all viewers.
     */
    @Override
    public void update() {
        this.viewers.forEach(p -> p.sendPacket(createNewWindowItemsPacket(p)));
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
        this.clickProcessor.clearCache(player);
        return result;
    }

    /**
     * Gets the cursor item of a player.
     *
     * @deprecated normal inventories no longer store cursor items
     * @see <a href="https://github.com/Minestom/Minestom/pull/2294">the relevant PR</a>
     */
    @Deprecated
    public @NotNull ItemStack getCursorItem(@NotNull Player player) {
        return player.getInventory().getCursorItem();
    }

    /**
     * Changes the cursor item of a player.
     *
     * @deprecated normal inventories no longer store cursor items
     * @see <a href="https://github.com/Minestom/Minestom/pull/2294">the relevant PR</a>
     */
    @Deprecated
    public void setCursorItem(@NotNull Player player, @NotNull ItemStack cursorItem) {
        player.getInventory().setCursorItem(cursorItem);
    }

    @Override
    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack, boolean sendPacket) {
        itemStacks[slot] = itemStack;
        if (sendPacket) sendPacketToViewers(new SetSlotPacket(getWindowId(), 0, (short) slot, itemStack));
    }

    private @NotNull WindowItemsPacket createNewWindowItemsPacket(Player player) {
        return new WindowItemsPacket(getWindowId(), 0, List.of(getItemStacks()), player.getInventory().getCursorItem());
    }

    /**
     * Sends a window property to all viewers.
     *
     * @param property the property to send
     * @param value    the value of the property
     * @see <a href="https://wiki.vg/Protocol#Window_Property">https://wiki.vg/Protocol#Window_Property</a>
     */
    protected void sendProperty(@NotNull InventoryProperty property, short value) {
        sendPacketToViewers(new WindowPropertyPacket(getWindowId(), property.getProperty(), value));
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = playerInventory.getCursorItem();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final InventoryClickResult clickResult = clickProcessor.leftClick(player,
                isInWindow ? this : playerInventory, clickSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            updateAll(player);
            return false;
        }
        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }
        playerInventory.setCursorItem(clickResult.getCursor());
        callClickEvent(player, isInWindow ? this : null, slot, ClickType.LEFT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = playerInventory.getCursorItem();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final InventoryClickResult clickResult = clickProcessor.rightClick(player,
                isInWindow ? this : playerInventory, clickSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            updateAll(player);
            return false;
        }
        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }
        playerInventory.setCursorItem(clickResult.getCursor());
        callClickEvent(player, isInWindow ? this : null, slot, ClickType.RIGHT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final ItemStack cursor = playerInventory.getCursorItem(); // Isn't used in the algorithm
        final InventoryClickResult clickResult = clickProcessor.shiftClick(
                isInWindow ? this : playerInventory,
                isInWindow ? playerInventory : this,
                0, isInWindow ? playerInventory.getInnerSize() : getInnerSize(), 1,
                player, clickSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            updateAll(player);
            return false;
        }
        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }
        updateAll(player); // FIXME: currently not properly client-predicted
        playerInventory.setCursorItem(clickResult.getCursor());
        return true;
    }

    @Override
    public boolean changeHeld(@NotNull Player player, int slot, int key) {
        final int convertedKey = key == 40 ? PlayerInventoryUtils.OFFHAND_SLOT : key;
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final ItemStack heldItem = playerInventory.getItemStack(convertedKey);
        final InventoryClickResult clickResult = clickProcessor.changeHeld(player,
                isInWindow ? this : playerInventory, clickSlot, convertedKey, clicked, heldItem);
        if (clickResult.isCancel()) {
            updateAll(player);
            return false;
        }
        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(clickSlot, clickResult.getClicked());
        }
        playerInventory.setItemStack(convertedKey, clickResult.getCursor());
        callClickEvent(player, isInWindow ? this : null, slot, ClickType.CHANGE_HELD, clicked, playerInventory.getCursorItem());
        return true;
    }

    @Override
    public boolean middleClick(@NotNull Player player, int slot) {
        // TODO
        update(player);
        return false;
    }

    @Override
    public boolean drop(@NotNull Player player, boolean all, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final boolean outsideDrop = slot == -999;
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = outsideDrop ?
                ItemStack.AIR : (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot));
        final ItemStack cursor = playerInventory.getCursorItem();
        final InventoryClickResult clickResult = clickProcessor.drop(player,
                isInWindow ? this : playerInventory, all, clickSlot, button, clicked, cursor);
        if (clickResult.isCancel()) {
            updateAll(player);
            return false;
        }
        final ItemStack resultClicked = clickResult.getClicked();
        if (!outsideDrop && resultClicked != null) {
            if (isInWindow) {
                setItemStack(slot, resultClicked);
            } else {
                playerInventory.setItemStack(clickSlot, resultClicked);
            }
        }
        playerInventory.setCursorItem(clickResult.getCursor());
        return true;
    }

    @Override
    public boolean dragging(@NotNull Player player, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = slot != -999 ?
                (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot)) :
                ItemStack.AIR;
        final ItemStack cursor = playerInventory.getCursorItem();
        final InventoryClickResult clickResult = clickProcessor.dragging(player,
                slot != -999 ? (isInWindow ? this : playerInventory) : null,
                clickSlot, button,
                clicked, cursor);
        if (clickResult == null || clickResult.isCancel()) {
            updateAll(player);
            return false;
        }
        playerInventory.setCursorItem(clickResult.getCursor());
        updateAll(player); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final ItemStack clicked = slot != -999 ?
                (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot)) :
                ItemStack.AIR;
        final ItemStack cursor = playerInventory.getCursorItem();
        final InventoryClickResult clickResult = clickProcessor.doubleClick(isInWindow ? this : playerInventory,
                this, player, clickSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            updateAll(player);
            return false;
        }
        playerInventory.setCursorItem(clickResult.getCursor());
        updateAll(player); // FIXME: currently not properly client-predicted
        return true;
    }

    private boolean isClickInWindow(int slot) {
        return slot < getSize();
    }

    private void updateAll(Player player) {
        player.getInventory().update();
        update(player);
    }
}
