package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ServerDataPacket(@Nullable Component motd, @Nullable String iconBase64,
                               boolean previewsChat, boolean enforcesSecureChat) implements ServerPacket {
    public ServerDataPacket(BinaryReader reader) {
        this(reader.readBoolean() ? reader.readComponent() : null, reader.readBoolean() ? reader.readSizedString() : null,
                reader.readBoolean(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(motd != null);
        if (motd != null) writer.writeComponent(motd);
        writer.writeBoolean(iconBase64 != null);
        if (iconBase64 != null) writer.writeSizedString(iconBase64);
        writer.writeBoolean(previewsChat);
        writer.writeBoolean(enforcesSecureChat);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SERVER_DATA;
    }
}
