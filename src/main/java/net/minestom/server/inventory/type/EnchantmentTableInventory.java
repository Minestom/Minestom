package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;

public class EnchantmentTableInventory extends Inventory {

    public EnchantmentTableInventory(String title) {
        super(InventoryType.ENCHANTMENT, title);
    }
}
