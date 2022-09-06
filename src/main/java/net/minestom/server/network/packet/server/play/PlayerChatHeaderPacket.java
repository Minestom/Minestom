package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.crypto.SignedMessageHeader;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record PlayerChatHeaderPacket(@NotNull SignedMessageHeader messageHeader, @NotNull MessageSignature signature,
                                     byte[] bodyDigest) implements ServerPacket {
    public PlayerChatHeaderPacket(BinaryReader reader) {
        this(new SignedMessageHeader(reader), new MessageSignature(reader), reader.readByteArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.write(messageHeader);
        writer.write(signature);
        writer.writeByteArray(bodyDigest);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_CHAT_HEADER;
    }
}
