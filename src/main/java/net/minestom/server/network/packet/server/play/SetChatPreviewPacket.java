package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record SetChatPreviewPacket(boolean enable) implements ServerPacket {
    public SetChatPreviewPacket(BinaryReader reader) {
        this(reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(enable);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_DISPLAY_CHAT_PREVIEW;
    }
}
