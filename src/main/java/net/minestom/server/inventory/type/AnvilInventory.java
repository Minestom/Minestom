package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;

public class AnvilInventory extends Inventory {

    public AnvilInventory(String title) {
        super(InventoryType.ANVIL, title);
    }

    public void setRepairCost(short cost) {
        sendProperty(InventoryProperty.ANVIL_REPAIR_COST, cost);
    }
}
