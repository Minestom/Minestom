package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientChatMessagePacket extends ClientPlayPacket {

    public String message = "";

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.message = reader.readSizedString(256);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        if(message.length() > 256) {
            throw new IllegalArgumentException("Message cannot be more than 256 characters long.");
        }
        writer.writeSizedString(message);
    }
}
