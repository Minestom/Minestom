package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class InventoryData extends DataType<Inventory> {

    @Override
    public void encode(PacketWriter packetWriter, Inventory value) {
        final InventoryType inventoryType = value.getInventoryType();
        final int size = inventoryType.getAdditionalSlot();

        // Inventory title & type
        packetWriter.writeSizedString(value.getTitle());
        packetWriter.writeSizedString(inventoryType.name());

        // Write all item stacks
        for (int i = 0; i < size; i++) {
            packetWriter.writeItemStack(value.getItemStack(i));
        }
    }

    @Override
    public Inventory decode(PacketReader packetReader) {
        final String title = packetReader.readSizedString();
        final InventoryType inventoryType = InventoryType.valueOf(packetReader.readSizedString());
        final int size = inventoryType.getAdditionalSlot();

        Inventory inventory = new Inventory(inventoryType, title);

        // Read all item stacks
        for (int i = 0; i < size; i++) {
            inventory.setItemStack(i, packetReader.readSlot());
        }

        return inventory;
    }
}
