package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;

public class FurnaceInventory extends Inventory {

    public FurnaceInventory(String title) {
        super(InventoryType.FURNACE, title);
    }

    /**
     * Represent the amount of tick until the fire icon come empty`
     *
     * @param remainingFuelTick
     */
    public void setRemainingFuelTick(short remainingFuelTick) {
        sendProperty(InventoryProperty.FURNACE_FIRE_ICON, remainingFuelTick);
    }

    public void setMaximumFuelBurnTime(short maximumFuelBurnTime) {
        sendProperty(InventoryProperty.FURNACE_MAXIMUM_FUEL_BURN_TIME, maximumFuelBurnTime);
    }

    public void setProgressArrow(short progressArrow) {
        sendProperty(InventoryProperty.FURNACE_PROGRESS_ARROW, progressArrow);
    }

    public void setMaximumProgress(short maximumProgress) {
        sendProperty(InventoryProperty.FURNACE_MAXIMUM_PROGRESS, maximumProgress);
    }
}
