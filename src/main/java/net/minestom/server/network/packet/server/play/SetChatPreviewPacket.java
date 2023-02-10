package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record SetChatPreviewPacket(boolean enable) implements ServerPacket {
    public SetChatPreviewPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BOOLEAN, enable);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_DISPLAY_CHAT_PREVIEW;
    }
}
