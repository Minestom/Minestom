package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.crypto.SignedMessageHeader;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;

public record PlayerChatHeaderPacket(@NotNull SignedMessageHeader messageHeader, @NotNull MessageSignature signature,
                                     byte[] bodyDigest) implements ServerPacket {
    public PlayerChatHeaderPacket(@NotNull NetworkBuffer reader) {
        this(new SignedMessageHeader(reader), new MessageSignature(reader), reader.read(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(messageHeader);
        writer.write(signature);
        writer.write(BYTE_ARRAY, bodyDigest);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_CHAT_HEADER;
    }
}
