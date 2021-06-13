package net.minestom.server.inventory;

public interface InventoryHolder<I extends AbstractInventory> {

	I getInventory();
}
