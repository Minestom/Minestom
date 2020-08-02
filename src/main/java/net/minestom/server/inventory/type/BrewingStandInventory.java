package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;

public class BrewingStandInventory extends Inventory {

    private short brewTime;
    private short fuelTime;

    public BrewingStandInventory(String title) {
        super(InventoryType.BREWING_STAND, title);
    }

    /**
     * Get the brewing stand brew time
     *
     * @return the brew time in tick
     */
    public short getBrewTime() {
        return brewTime;
    }

    /**
     * Change the brew time
     *
     * @param brewTime the new brew time in tick
     */
    public void setBrewTime(short brewTime) {
        this.brewTime = brewTime;
        sendProperty(InventoryProperty.BREWING_STAND_BREW_TIME, brewTime);
    }

    /**
     * Get the brewing stand fuel time
     *
     * @return the fuel time in tick
     */
    public short getFuelTime() {
        return fuelTime;
    }

    /**
     * Change the fuel time
     *
     * @param fuelTime the new fuel time in tick
     */
    public void setFuelTime(short fuelTime) {
        this.fuelTime = fuelTime;
        sendProperty(InventoryProperty.BREWING_STAND_FUEL_TIME, fuelTime);
    }

}
