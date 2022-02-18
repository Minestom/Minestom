package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientNameItemPacket(@NotNull String itemName) implements ClientPacket {
    public ClientNameItemPacket {
        if (itemName.length() > Short.MAX_VALUE) {
            throw new IllegalArgumentException("ItemStack name cannot be longer than Short.MAX_VALUE characters!");
        }
    }

    public ClientNameItemPacket(BinaryReader reader) {
        this(reader.readSizedString(Short.MAX_VALUE));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(itemName);
    }
}
