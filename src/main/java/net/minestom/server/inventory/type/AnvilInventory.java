package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.ContainerInventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class AnvilInventory extends ContainerInventory {

    private short repairCost;

    public AnvilInventory(@NotNull Component title) {
        super(InventoryType.ANVIL, title);
    }

    public AnvilInventory(@NotNull String title) {
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
