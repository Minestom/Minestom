package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record DeleteChatPacket(@NotNull MessageSignature signature) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DeleteChatPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, DeleteChatPacket value) {
            writer.write(value.signature);
        }

        @Override
        public DeleteChatPacket read(@NotNull NetworkBuffer reader) {
            return new DeleteChatPacket(new MessageSignature(reader));
        }
    };
}
