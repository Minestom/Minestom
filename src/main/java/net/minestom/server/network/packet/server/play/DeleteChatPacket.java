package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record DeleteChatPacket(@NotNull MessageSignature signature) implements ServerPacket.Play {
    public DeleteChatPacket(@NotNull NetworkBuffer reader) {
        this(new MessageSignature(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(signature);
    }

}
