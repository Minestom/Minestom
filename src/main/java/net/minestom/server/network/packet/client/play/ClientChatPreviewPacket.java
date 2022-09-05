package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientChatPreviewPacket(int queryId, @NotNull String query) implements ClientPacket {
    public ClientChatPreviewPacket(BinaryReader reader) {
        this(reader.readInt(), reader.readSizedString(256));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(queryId);
        writer.writeSizedString(query);
    }
}
