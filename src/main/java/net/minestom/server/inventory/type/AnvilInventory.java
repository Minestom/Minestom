package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;

public class AnvilInventory extends Inventory {

    private short repairCost;

    public AnvilInventory(String title) {
        super(InventoryType.ANVIL, title);
    }

    /**
     * Gets the anvil repair cost.
     *
     * @return the repair cost
     */
    public short getRepairCost() {
        return repairCost;
    }

    /**
     * Sets the anvil repair cost.
     *
     * @param cost the new anvil repair cost
     */
    public void setRepairCost(short cost) {
        this.repairCost = cost;
        sendProperty(InventoryProperty.ANVIL_REPAIR_COST, cost);
    }
}
