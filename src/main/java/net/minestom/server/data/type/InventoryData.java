package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class InventoryData extends DataType<Inventory> {

    @Override
    public void encode(BinaryWriter binaryWriter, Inventory value) {
        final InventoryType inventoryType = value.getInventoryType();
        final int size = inventoryType.getAdditionalSlot();

        // Inventory title & type
        binaryWriter.writeSizedString(value.getTitle());
        binaryWriter.writeSizedString(inventoryType.name());

        // Write all item stacks
        for (int i = 0; i < size; i++) {
            binaryWriter.writeItemStack(value.getItemStack(i));
        }
    }

    @Override
    public Inventory decode(BinaryReader binaryReader) {
        final String title = binaryReader.readSizedString();
        final InventoryType inventoryType = InventoryType.valueOf(binaryReader.readSizedString());
        final int size = inventoryType.getAdditionalSlot();

        Inventory inventory = new Inventory(inventoryType, title);

        // Read all item stacks
        for (int i = 0; i < size; i++) {
            inventory.setItemStack(i, binaryReader.readSlot());
        }

        return inventory;
    }
}
