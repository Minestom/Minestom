package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientNameItemPacket extends ClientPlayPacket {

    public String itemName = "";

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.itemName = reader.readSizedString(Short.MAX_VALUE);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        if(itemName.length() > Short.MAX_VALUE) {
            throw new IllegalArgumentException("ItemStack name cannot be longer than Short.MAX_VALUE characters!");
        }
        writer.writeSizedString(itemName);
    }
}
