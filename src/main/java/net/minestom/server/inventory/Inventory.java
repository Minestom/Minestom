package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.WindowPropertyPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an inventory which can be viewed by a collection of {@link Player}.
 * <p>
 * You can create one with {@link Inventory#Inventory(InventoryType, String)} or by making your own subclass.
 * It can then be opened using {@link Player#openInventory(ViewableInventory)}.
 */
public non-sealed class Inventory extends ViewableInventory {
    private final InventoryType inventoryType;
    private Component title;

    public Inventory(InventoryType inventoryType, Component title) {
        super(inventoryType.getSize());
        this.inventoryType = inventoryType;
        this.title = title;
    }

    public Inventory(InventoryType inventoryType, String title) {
        this(inventoryType, Component.text(title));
    }

    /**
     * Gets the inventory type.
     *
     * @return the inventory type
     */
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    /**
     * Gets the inventory title.
     *
     * @return the inventory title
     */
    public Component getTitle() {
        return title;
    }

    /**
     * Changes the inventory title.
     *
     * @param title the new inventory title
     */
    public void setTitle(Component title) {
        this.title = title;
        // Re-open the inventory
        sendPacketToViewers(getOpenPacket());
        // Send inventory items
        update();
    }

    @Override
    SendablePacket getOpenPacket() {
        return new OpenWindowPacket(id, getInventoryType().getWindowType(), title);
    }
}
