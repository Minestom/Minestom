package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * It can then be opened using {@link Player#openInventory(ViewableInventory)}.
 */
public sealed abstract class ViewableInventory extends AbstractInventory permits Inventory, HorseInventory {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    final byte id;

    private final int offset;

    public ViewableInventory(int size) {
        super(size);
        this.id = generateId();

        this.offset = getSize();
    }

    private static byte generateId() {
        return (byte) ID_COUNTER.updateAndGet(i -> i + 1 >= 128 ? 1 : i + 1);
    }

    @Override
    public byte getWindowId() {
        return id;
    }

    /**
     * This will not open the inventory for {@code player}, use {@link Player#openInventory(ViewableInventory)}.
     *
     * @param player the viewer to add
     * @return true if the player has successfully been added
     */
    @Override
    public boolean addViewer(Player player) {
        if (!this.viewers.add(player)) return false;

        // Also send the open window packet
        player.sendPacket(getOpenPacket());
        update(player);
        return true;
    }

    abstract SendablePacket getOpenPacket();

    /**
     * This will not close the inventory for {@code player}, use {@link Player#closeInventory()}.
     *
     * @param player the viewer to remove
     * @return true if the player has successfully been removed
     */
    @Override
    public boolean removeViewer(Player player) {
        if (!super.removeViewer(player)) return false;

        player.getClickPreprocessor().clearCache();
        return true;
    }

    /**
     * Gets the cursor item of a player.
     *
     * @deprecated normal inventories no longer store cursor items
     * @see <a href="https://github.com/Minestom/Minestom/pull/2294">the relevant PR</a>
     */
    @Deprecated
    public ItemStack getCursorItem(Player player) {
        return player.getInventory().getCursorItem();
    }

    /**
     * Changes the cursor item of a player.
     *
     * @deprecated normal inventories no longer store cursor items
     * @see <a href="https://github.com/Minestom/Minestom/pull/2294">the relevant PR</a>
     */
    @Deprecated
    public void setCursorItem(Player player, ItemStack cursorItem) {
        player.getInventory().setCursorItem(cursorItem);
    }

    /**
     * Sends a window property to all viewers.
     *
     * @param property the property to send
     * @param value    the value of the property
     * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Set_Container_Property">the Minecraft wiki</a>
     */
    protected void sendProperty(InventoryProperty property, short value) {
        sendPacketToViewers(new WindowPropertyPacket(getWindowId(), property.getProperty(), value));
    }

    @Override
    public boolean leftClick(Player player, int slot) {
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
    public boolean rightClick(Player player, int slot) {
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
    public boolean shiftClick(Player player, int slot, int button) {
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

        updateAll(player); // FIXME: currently not properly client-predicted
        playerInventory.setCursorItem(clickResult.getCursor());
        return true;
    }

    @Override
    public boolean changeHeld(Player player, int slot, int key) {
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
    public boolean middleClick(Player player, int slot) {
        // TODO
        update(player);
        return false;
    }

    @Override
    public boolean drop(Player player, boolean all, int slot) {
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
    public boolean dragging(Player player, List<Integer> slots, int button) {
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
    public boolean doubleClick(Player player, int slot) {
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
