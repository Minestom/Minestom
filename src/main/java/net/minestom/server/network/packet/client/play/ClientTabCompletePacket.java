package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientTabCompletePacket(int transactionId, @NotNull String text) implements ClientPacket {
    public ClientTabCompletePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(STRING));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeSizedString(text);
    }
}
