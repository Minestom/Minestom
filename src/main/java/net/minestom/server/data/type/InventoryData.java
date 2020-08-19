package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class InventoryData extends DataType<Inventory> {

    @Override
    public void encode(BinaryWriter writer, Inventory value) {
        final InventoryType inventoryType = value.getInventoryType();
        final int size = inventoryType.getAdditionalSlot();

        // Inventory title & type
        writer.writeSizedString(value.getTitle());
        writer.writeSizedString(inventoryType.name());

        // Write all item stacks
        for (int i = 0; i < size; i++) {
            writer.writeItemStack(value.getItemStack(i));
        }
    }

    @Override
    public Inventory decode(BinaryReader reader) {
        final String title = reader.readSizedString();
        final InventoryType inventoryType = InventoryType.valueOf(reader.readSizedString());
        final int size = inventoryType.getAdditionalSlot();

        Inventory inventory = new Inventory(inventoryType, title);

        // Read all item stacks
        for (int i = 0; i < size; i++) {
            inventory.setItemStack(i, reader.readSlot());
        }

        return inventory;
    }
}
