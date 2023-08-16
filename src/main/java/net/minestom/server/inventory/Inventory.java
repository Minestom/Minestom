package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link Inventory#Inventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(AbstractInventory)}.
 */
public class Inventory extends AbstractInventory {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private final byte id;
    private final InventoryType inventoryType;
    private Component title;

    public Inventory(@NotNull InventoryType inventoryType, @NotNull Component title) {
        super(inventoryType.getSize());
        this.id = generateId();
        this.inventoryType = inventoryType;
        this.title = title;
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

        // Reopen and update this inventory with the new title
        sendPacketToViewers(new OpenWindowPacket(getWindowId(), getInventoryType().getWindowType(), title));
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
    public void refreshSlot(int slot, @NotNull ItemStack itemStack) {
        super.refreshSlot(slot, itemStack);
        sendPacketToViewers(new SetSlotPacket(getWindowId(), 0, (short) slot, itemStack));
    }

    @Override
    public void handleOpen(@NotNull Player player) {
        player.sendPacket(new OpenWindowPacket(getWindowId(), inventoryType.getWindowType(), getTitle()));
        super.handleOpen(player);
    }

    @Override
    public void handleClose(@NotNull Player player) {
        super.handleClose(player);
        player.sendPacket(new CloseWindowPacket(getWindowId()));
    }

    @Override
    public void update(@NotNull Player player) {
        super.update(player);
        player.sendPacket(new WindowItemsPacket(getWindowId(), 0, List.of(itemStacks), cursorPlayersItem.getOrDefault(player, ItemStack.AIR)));
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

}
