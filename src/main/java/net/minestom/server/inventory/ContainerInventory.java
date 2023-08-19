package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link ContainerInventory#ContainerInventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(Inventory)}.
 */
public non-sealed class ContainerInventory extends InventoryImpl implements Inventory.Typed {
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

    @Override
    public @NotNull InventoryType getInventoryType() {
        return inventoryType;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;

        // Reopen and update this inventory with the new title
        sendPacketToViewers(new OpenWindowPacket(getWindowId(), getInventoryType().getWindowType(), title));
        update();
    }

    @Override
    public byte getWindowId() {
        return id;
    }

    @Override
    public void handleOpen(@NotNull Player player) {
        player.sendPacket(new OpenWindowPacket(getWindowId(), inventoryType.getWindowType(), getTitle()));
        super.handleOpen(player);
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
