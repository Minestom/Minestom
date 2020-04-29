package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;

public class BeaconInventory extends Inventory {

    public BeaconInventory(String title) {
        super(InventoryType.BEACON, title);
    }

    public void setPowerLevel(short powerLevel) {
        sendProperty(InventoryProperty.BEACON_POWER_LEVEL, powerLevel);
    }

    public void setFirstPotionEffect(short firstPotionEffect) {
        sendProperty(InventoryProperty.BEACON_FIRST_POTION, firstPotionEffect);
    }

    public void setSecondPotionEffect(short secondPotionEffect) {
        sendProperty(InventoryProperty.BEACON_SECOND_POTION, secondPotionEffect);
    }

}
