package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record DeleteChatPacket(@NotNull MessageSignature signature) implements ServerPacket {
    public DeleteChatPacket(BinaryReader reader) {
        this(new MessageSignature(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.write(signature);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DELETE_CHAT_MESSAGE;
    }
}
