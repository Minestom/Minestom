package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class InventoryData extends DataType<Inventory> {

    @Override
    public void encode(PacketWriter packetWriter, Inventory value) {
        InventoryType inventoryType = value.getInventoryType();
        int size = inventoryType.getAdditionalSlot();

        packetWriter.writeSizedString(value.getTitle());
        packetWriter.writeSizedString(inventoryType.name());

        for (int i = 0; i < size; i++) {
            packetWriter.writeItemStack(value.getItemStack(i));
        }
    }

    @Override
    public Inventory decode(PacketReader packetReader) {
        String title = packetReader.readSizedString();
        InventoryType inventoryType = InventoryType.valueOf(packetReader.readSizedString());
        int size = inventoryType.getAdditionalSlot();

        Inventory inventory = new Inventory(inventoryType, title);

        for (int i = 0; i < size; i++) {
            inventory.setItemStack(i, packetReader.readSlot());
        }

        return inventory;
    }
}
