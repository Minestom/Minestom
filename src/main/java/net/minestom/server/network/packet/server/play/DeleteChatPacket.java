package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

public record DeleteChatPacket(@NotNull MessageSignature signature) implements ServerPacket {
    public DeleteChatPacket(@NotNull NetworkBuffer reader) {
        this(new MessageSignature(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(signature);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.DELETE_CHAT_MESSAGE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
