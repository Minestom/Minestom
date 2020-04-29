package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;

public class BrewingStandInventory extends Inventory {

    public BrewingStandInventory(String title) {
        super(InventoryType.BREWING_STAND, title);
    }

    public void setBrewTime(short brewTime) {
        sendProperty(InventoryProperty.BREWING_STAND_BREW_TIME, brewTime);
    }

    public void setFuelTime(short fuelTime) {
        sendProperty(InventoryProperty.BREWING_STAND_FUEL_TIME, fuelTime);
    }

}
