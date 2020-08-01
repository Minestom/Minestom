package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;

public class FurnaceInventory extends Inventory {

    private short remainingFuelTick;
    private short maximumFuelBurnTime;
    private short progressArrow;
    private short maximumProgress;

    public FurnaceInventory(String title) {
        super(InventoryType.FURNACE, title);
    }

    /**
     * Represent the amount of tick until the fire icon come empty
     *
     * @return the amount of tick until the fire icon come empty
     */
    public short getRemainingFuelTick() {
        return remainingFuelTick;
    }

    /**
     * Represent the amount of tick until the fire icon come empty
     *
     * @param remainingFuelTick
     */
    public void setRemainingFuelTick(short remainingFuelTick) {
        this.remainingFuelTick = remainingFuelTick;
        sendProperty(InventoryProperty.FURNACE_FIRE_ICON, remainingFuelTick);
    }

    public short getMaximumFuelBurnTime() {
        return maximumFuelBurnTime;
    }

    public void setMaximumFuelBurnTime(short maximumFuelBurnTime) {
        this.maximumFuelBurnTime = maximumFuelBurnTime;
        sendProperty(InventoryProperty.FURNACE_MAXIMUM_FUEL_BURN_TIME, maximumFuelBurnTime);
    }

    public short getProgressArrow() {
        return progressArrow;
    }

    public void setProgressArrow(short progressArrow) {
        this.progressArrow = progressArrow;
        sendProperty(InventoryProperty.FURNACE_PROGRESS_ARROW, progressArrow);
    }

    public short getMaximumProgress() {
        return maximumProgress;
    }

    public void setMaximumProgress(short maximumProgress) {
        this.maximumProgress = maximumProgress;
        sendProperty(InventoryProperty.FURNACE_MAXIMUM_PROGRESS, maximumProgress);
    }
}
