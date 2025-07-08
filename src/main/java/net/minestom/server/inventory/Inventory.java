package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link Inventory#Inventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(Inventory)}.
 */
public non-sealed class Inventory extends AbstractInventory {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private final byte id;
    private final InventoryType inventoryType;
    private Component title;

    private final int offset;

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

    @Override
    public byte getWindowId() {
        return id;
    }

    /**
     * This will not open the inventory for {@code player}, use {@link Player#openInventory(Inventory)}.
     *
     * @param player the viewer to add
     * @return true if the player has successfully been added
     */
    @Override
    public boolean addViewer(@NotNull Player player) {
        if (!this.viewers.add(player)) return false;

        // Also send the open window packet
        player.sendPacket(new OpenWindowPacket(id, getInventoryType().getWindowType(), title));
        update(player);
        return true;
    }

    /**
     * This will not close the inventory for {@code player}, use {@link Player#closeInventory()}.
     *
     * @param player the viewer to remove
     * @return true if the player has successfully been removed
     */
    @Override
    public boolean removeViewer(@NotNull Player player) {
        if (!super.removeViewer(player)) return false;

        player.getClickPreprocessor().clearCache();
        return true;
    }

    /**
     * Gets the cursor item of a player.
     *
     * @see <a href="https://github.com/Minestom/Minestom/pull/2294">the relevant PR</a>
     * @deprecated normal inventories no longer store cursor items
     */
    @Deprecated
    public @NotNull ItemStack getCursorItem(@NotNull Player player) {
        return player.getInventory().getCursorItem();
    }

    /**
     * Changes the cursor item of a player.
     *
     * @see <a href="https://github.com/Minestom/Minestom/pull/2294">the relevant PR</a>
     * @deprecated normal inventories no longer store cursor items
     */
    @Deprecated
    public void setCursorItem(@NotNull Player player, @NotNull ItemStack cursorItem) {
        player.getInventory().setCursorItem(cursorItem);
    }

    /**
     * Sends a window property to all viewers.
     *
     * @param property the property to send
     * @param value    the value of the property
     * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Set_Container_Property">the Minecraft wiki</a>
     */
    protected void sendProperty(@NotNull InventoryProperty property, short value) {
        sendPacketToViewers(new WindowPropertyPacket(getWindowId(), property.getProperty(), value));
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = playerInventory.getCursorItem();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : slot - offset;
        final AbstractInventory clickedInventory = isInWindow ? this : playerInventory;
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final InventoryClickResult clickResult = clickProcessor.leftClick(clicked, cursor);
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
        callClickEvent(player, clickedInventory, slot, ClickType.LEFT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = playerInventory.getCursorItem();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : slot - offset;
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final AbstractInventory clickedInventory = isInWindow ? this : playerInventory;
        final InventoryClickResult clickResult = clickProcessor.rightClick(clicked, cursor);
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
        callClickEvent(player, clickedInventory, slot, ClickType.RIGHT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : slot - offset;
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final ItemStack cursor = playerInventory.getCursorItem(); // Isn't used in the algorithm

        InventoryClickResult clickResult;

        if (isInWindow) {
            // The player shift-clicked an item in this GUI into their inventory.
            // Prioritize the hotbar (8->0), then their regular inventory (35->9).
            clickResult = clickProcessor.shiftClick(
                    this, playerInventory,
                    8, 0, -1,
                    player, clickSlot, clicked, cursor);

            if (clickResult.isCancel()) {
                clickResult = clickProcessor.shiftClick(
                        this, playerInventory,
                        playerInventory.getInnerSize() - 1, 0, -1,
                        player, clickSlot, clicked, cursor);
            }
        } else {
            clickResult = clickProcessor.shiftClick(
                    playerInventory, this,
                    0, getInnerSize(), 1,
                    player, clickSlot, clicked, cursor);
        }

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
        return true;
    }

    @Override
    public boolean changeHeld(@NotNull Player player, int slot, int key) {
        final int convertedKey = key == 40 ? PlayerInventoryUtils.OFFHAND_SLOT : key;
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : slot - offset;
        final ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot);
        final ItemStack heldItem = playerInventory.getItemStack(convertedKey);
        final AbstractInventory clickedInventory = isInWindow ? this : playerInventory;
        final InventoryClickResult clickResult = clickProcessor.changeHeld(clicked, heldItem);
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
        callClickEvent(player, clickedInventory, slot, ClickType.CHANGE_HELD, clicked, playerInventory.getCursorItem());
        return true;
    }

    @Override
    public boolean middleClick(@NotNull Player player, int slot) {
        // TODO
        update(player);
        return false;
    }

    @Override
    public boolean drop(@NotNull Player player, boolean all, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final boolean outsideDrop = slot == -999;
        final int clickSlot = isInWindow ? slot : slot - offset;
        final ItemStack clicked = outsideDrop ?
                ItemStack.AIR : (isInWindow ? getItemStack(slot) : playerInventory.getItemStack(clickSlot));
        final ItemStack cursor = playerInventory.getCursorItem();
        final InventoryClickResult clickResult = clickProcessor.drop(player, all, clickSlot, clicked, cursor);
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
    public boolean dragging(@NotNull Player player, List<Integer> slots, int button) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack cursor = playerInventory.getCursorItem();

        final ItemStack clickResult = clickProcessor.dragging(player, this, slots, button, cursor);
        if (clickResult == null) {
            updateAll(player);
            return false;
        }
        playerInventory.setCursorItem(clickResult);
        updateAll(player); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : slot - offset;
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
