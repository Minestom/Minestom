package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientTabCompletePacket(int transactionId, @NotNull String text) implements ClientPacket {
    public ClientTabCompletePacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readSizedString(Short.MAX_VALUE));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeSizedString(text);
    }
}
