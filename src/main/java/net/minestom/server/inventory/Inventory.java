package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickProcessor;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.DragHelper;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.OFFHAND_SLOT;

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

    private static byte generateId() {
        return (byte) Math.abs((byte) ID_COUNTER.incrementAndGet());
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

    @Override
    public synchronized void clear() {
        this.cursorPlayersItem.clear();
        super.clear();
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
        setCursorItem(player, ItemStack.AIR);
        this.dragHelper.clearCache(player);
        return result;
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
        final ItemStack currentCursorItem = cursorPlayersItem.getOrDefault(player, ItemStack.AIR);
        if (!currentCursorItem.equals(cursorItem)) {
            player.sendPacket(SetSlotPacket.createCursorPacket(cursorItem));
        }
        if (!cursorItem.isAir()) {
            this.cursorPlayersItem.put(player, cursorItem);
        } else {
            this.cursorPlayersItem.remove(player);
        }
    }

    @Override
    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack, boolean sendPacket) {
        itemStacks[slot] = itemStack;
        if (sendPacket) sendPacketToViewers(new SetSlotPacket(getWindowId(), 0, (short) slot, itemStack));
    }

    private @NotNull WindowItemsPacket createNewWindowItemsPacket(Player player) {
        return new WindowItemsPacket(getWindowId(), 0, List.of(getItemStacks()), cursorPlayersItem.getOrDefault(player, ItemStack.AIR));
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
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        var inventory = isInWindow ? this : player.getInventory();
        final var tmp = handlePreClick(inventory, player, clickSlot, ClickType.LEFT_CLICK, getCursorItem(player), inventory.getItemStack(clickSlot));
        if (tmp.cancelled()) {
            update();
            return false;
        }
        return handleResult(ClickProcessor.left(clickSlot, tmp.clicked(), tmp.cursor()),
                itemStack -> setCursorItem(player, itemStack), player, inventory, ClickType.LEFT_CLICK);
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        var inventory = isInWindow ? this : player.getInventory();
        final var tmp = handlePreClick(inventory, player, clickSlot, ClickType.RIGHT_CLICK, getCursorItem(player), inventory.getItemStack(clickSlot));
        if (tmp.cancelled()) {
            update();
            return false;
        }
        return handleResult(ClickProcessor.right(clickSlot, tmp.clicked(), tmp.cursor()),
                itemStack -> setCursorItem(player, itemStack), player, inventory, ClickType.RIGHT_CLICK);
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot) {
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        PlayerInventory playerInv = player.getInventory();
        if (isInWindow) {
            final ItemStack item = getItemStack(clickSlot);
            return handleResult(ClickProcessor.shiftToPlayer(playerInv, item),
                    itemStack -> setItemStack(clickSlot, itemStack), player, playerInv, ClickType.SHIFT_CLICK);
        } else {
            final ItemStack item = playerInv.getItemStack(clickSlot);
            return handleResult(ClickProcessor.shiftToInventory(this, item),
                    itemStack -> playerInv.setItemStack(clickSlot, itemStack), player, this, ClickType.SHIFT_CLICK);
        }
    }

    @Override
    public boolean changeHeld(@NotNull Player player, int slot, int key) {
        final PlayerInventory playerInventory = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final int convertedKey = key == 40 ? OFFHAND_SLOT : key;
        final var clickInv = isInWindow ? this : playerInventory;
        final var tmp = handlePreClick(clickInv, player, clickSlot, ClickType.CHANGE_HELD,
                getCursorItem(player), clickInv.getItemStack(clickSlot));
        if (tmp.cancelled()) {
            update();
            return false;
        }
        return handleResult(ClickProcessor.held(playerInventory, clickInv, clickSlot, tmp.clicked(), convertedKey, playerInventory.getItemStack(convertedKey)),
                itemStack -> clickInv.setItemStack(clickSlot, itemStack), player, playerInventory, ClickType.SHIFT_CLICK);
    }

    @Override
    public boolean middleClick(@NotNull Player player, int slot) {
        // TODO
        update(player);
        return false;
    }

    @Override
    public boolean drop(@NotNull Player player, boolean all, int slot, int button) {
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        var inv = isInWindow ? this : player.getInventory();
        final ItemStack cursor = getCursorItem(player);
        final boolean outsideDrop = slot == -999;
        final ItemStack clicked = outsideDrop ? ItemStack.AIR : inv.getItemStack(clickSlot);
        var drop = ClickProcessor.drop(all, slot, button, clicked, cursor);

        player.dropItem(drop.drop());
        if (outsideDrop) {
            setCursorItem(player, drop.remaining());
        } else {
            inv.setItemStack(clickSlot, drop.remaining());
        }
        // TODO events
        return true;
    }

    private final DragHelper dragHelper = new DragHelper();

    @Override
    public boolean dragging(@NotNull Player player, int slot, int button) {
        var playerInv = player.getInventory();
        final boolean isInWindow = isClickInWindow(slot);
        final int clickSlot = isInWindow ? slot : PlayerInventoryUtils.convertSlot(slot, offset);
        final var clickInv = isInWindow ? this : playerInv;
        return dragHelper.test(player, slot, button, clickSlot, clickInv,
                // Start
                (clickType) -> {
                    final var tmp = handlePreClick(clickInv, player, -999, clickType,
                            getCursorItem(player), ItemStack.AIR);
                    if (tmp.cancelled()) {
                        update();
                        return false;
                    }
                    return true;
                },
                // Step
                (clickType) -> {
                    final var tmp = handlePreClick(clickInv, player, clickSlot, clickType,
                            getCursorItem(player), getItemStack(clickSlot));
                    return !tmp.cancelled();
                },
                // End
                (clickType, entries) -> {
                    var slots = entries.stream().map(dragData -> Pair.of(dragData.inventory(), dragData.slot())).toList();
                    // Handle last drag
                    {
                        final int lastSlot = entries.get(entries.size() - 1).slot();
                        final var tmp = handlePreClick(clickInv, player, lastSlot, clickType,
                                getCursorItem(player), getItemStack(lastSlot));
                        if (tmp.cancelled()) {
                            update();
                            return false;
                        }
                    }
                    if (clickType == ClickType.LEFT_DRAGGING) {
                        return handleResult(ClickProcessor.leftDrag(playerInv, this, getCursorItem(player), slots),
                                itemStack -> setCursorItem(player, itemStack), player, clickType);
                    } else {
                        return handleResult(ClickProcessor.rightDrag(playerInv, this, getCursorItem(player), slots),
                                itemStack -> setCursorItem(player, itemStack), player, clickType);
                    }
                });
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        return handleResult(ClickProcessor.doubleClick(player.getInventory(), this, getCursorItem(player)),
                itemStack -> setCursorItem(player, itemStack), player, ClickType.DOUBLE_CLICK);
    }

    private boolean handleResult(ClickResult.Double result, Consumer<ItemStack> remainingSetter,
                                 Player player, ClickType clickType) {
        // Player changes
        {
            var inv = player.getInventory();
            Map<Integer, ItemStack> playerChanges = result.playerChanges();
            playerChanges.forEach((slot, itemStack) -> {
                // TODO call events (conditions/pre-click)
            });

            playerChanges.forEach((slot, itemStack) -> {
                inv.setItemStack(slot, itemStack);
                callClickEvent(player, null, slot, clickType, itemStack, getCursorItem(player));
            });
        }
        // This inventory changes
        {
            Map<Integer, ItemStack> playerChanges = result.inventoryChanges();
            playerChanges.forEach((slot, itemStack) -> {
                // TODO call events (conditions/pre-click)
            });

            playerChanges.forEach((slot, itemStack) -> {
                setItemStack(slot, itemStack);
                callClickEvent(player, this, slot, clickType, itemStack, getCursorItem(player));
            });
        }
        remainingSetter.accept(result.remaining());
        return true;
    }

    private boolean handleResult(ClickResult.Single result, Consumer<ItemStack> remainingSetter,
                                 Player player, AbstractInventory inventory, ClickType clickType) {
        Inventory eventInv = inventory instanceof Inventory ? ((Inventory) inventory) : null;
        Map<Integer, ItemStack> changes = result.changedSlots();
        changes.forEach((slot, itemStack) -> {
            // TODO call events (conditions/pre-click)
        });

        changes.forEach((slot, itemStack) -> {
            inventory.setItemStack(slot, itemStack);
            callClickEvent(player, eventInv, slot, clickType, itemStack, getCursorItem(player));
        });
        remainingSetter.accept(result.remaining());
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
