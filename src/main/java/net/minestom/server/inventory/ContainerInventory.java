package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPostClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickProcessors;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.utils.inventory.ClickUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link ContainerInventory#ContainerInventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(Inventory)}.
 */
public non-sealed class ContainerInventory extends InventoryImpl {

    /**
     * Processes a click, returning a result. This will call events for the click.
     *
     * @param inventory the clicked inventory (could be a player inventory)
     * @param player    the player who clicked
     * @param info      the click info describing the click
     * @return the click result, or null if the click did not occur
     */
    public static @Nullable List<Click.Change> handleClick(@NotNull Inventory inventory, @NotNull Player player, @NotNull Click.Info info,
                                                           @NotNull ClickProcessors.InventoryProcessor processor) {
        PlayerInventory playerInventory = player.getInventory();

        InventoryPreClickEvent preClickEvent = new InventoryPreClickEvent(playerInventory, inventory, player, info);
        EventDispatcher.call(preClickEvent);
        if (preClickEvent.isCancelled()) return null;

        final Click.Info newInfo = preClickEvent.getClickInfo();
        final List<Click.Change> changes = processor.apply(newInfo, ClickUtils.makeGetter(inventory, playerInventory));

        InventoryClickEvent clickEvent = new InventoryClickEvent(playerInventory, inventory, player, newInfo, changes);
        EventDispatcher.call(clickEvent);
        if (clickEvent.isCancelled()) return null;

        final List<Click.Change> newChanges = clickEvent.getChanges();

        apply(newChanges, player, inventory);

        EventDispatcher.call(new InventoryPostClickEvent(playerInventory, player, inventory, newInfo, newChanges));

        return newChanges;
    }

    public static void apply(@NotNull List<Click.Change> changes, @NotNull Player player, @NotNull Inventory inventory) {
        PlayerInventory playerInventory = player.getInventory();

        for (var change : changes) {
            switch (change) {
                case Click.Change.Container(int slot, ItemStack item) -> {
                    if (slot < inventory.getSize()) {
                        inventory.setItemStack(slot, item);
                    } else {
                        int converted = PlayerInventoryUtils.protocolToMinestom(slot, inventory.getSize());
                        playerInventory.setItemStack(converted, item);
                    }
                }
                case Click.Change.Player(int slot, ItemStack item) -> playerInventory.setItemStack(slot, item);
                case Click.Change.Cursor(ItemStack item) -> playerInventory.setCursorItem(item);
                case Click.Change.DropFromPlayer(ItemStack item) -> {
                    if (!player.dropItem(item)) {
                        playerInventory.addItemStack(item, TransactionOption.ALL);
                    }
                }
            }
        }
    }

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private final byte id;
    private final InventoryType inventoryType;
    private Component title;

    public ContainerInventory(@NotNull InventoryType inventoryType, @NotNull Component title) {
        super(inventoryType.getSize());
        this.id = generateId();
        this.inventoryType = inventoryType;
        this.title = title;
    }

    public ContainerInventory(@NotNull InventoryType inventoryType, @NotNull String title) {
        this(inventoryType, Component.text(title));
    }

    private static byte generateId() {
        return (byte) ID_COUNTER.updateAndGet(i -> i + 1 >= 128 ? 1 : i + 1);
    }

    /**
     * Gets the inventory type of this inventory.
     *
     * @return the inventory type
     */
    public @NotNull InventoryType getInventoryType() {
        return inventoryType;
    }

    /**
     * Gets the inventory title of this inventory.
     *
     * @return the inventory title
     */
    public @NotNull Component getTitle() {
        return title;
    }

    /**
     * Changes the inventory title of this inventory.
     *
     * @param title the new inventory title
     */
    public void setTitle(@NotNull Component title) {
        this.title = title;

        // Reopen and update this inventory with the new title
        sendPacketToViewers(new OpenWindowPacket(getWindowId(), getInventoryType().getWindowType(), title));
        update();
    }

    @Override
    public @Nullable List<Click.Change> handleClick(@NotNull Player player, Click.@NotNull Info info, @Nullable List<Click.Change> clientPrediction) {
        // We can use the client prediction if it's conservative (i.e. doesn't create or delete items) or the client is in creative.
        // Otherwise, we make our own.
        List<Click.Change> changes;
        if (clientPrediction != null && (ClickUtils.conservative(clientPrediction, this, player.getInventory()) || player.getGameMode() == GameMode.CREATIVE)) {
            changes = ContainerInventory.handleClick(this, player, info, (i, g) -> clientPrediction);
        } else {
            changes = ContainerInventory.handleClick(this, player, info,
                    ClickProcessors.PROCESSORS_MAP.getOrDefault(inventoryType, ClickProcessors.GENERIC_PROCESSOR));
        }

        if (changes == null || !changes.equals(clientPrediction)) {
            update(player);
            player.getInventory().update(player);
        }
        return changes;
    }

    @Override
    public byte getWindowId() {
        return id;
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        if (!this.viewers.add(player)) return false;

        player.sendPacket(new OpenWindowPacket(getWindowId(), inventoryType.getWindowType(), getTitle()));
        update(player);
        return true;
    }

    /**
     * Sends a window property to all viewers.
     *
     * @param property the property to send
     * @param value    the value of the property
     * @see <a href="https://wiki.vg/Protocol#Set_Container_Property">https://wiki.vg/Protocol#Set_Container_Property</a>
     */
    protected void sendProperty(@NotNull InventoryProperty property, short value) {
        sendPacketToViewers(new WindowPropertyPacket(getWindowId(), property.getProperty(), value));
    }
}
