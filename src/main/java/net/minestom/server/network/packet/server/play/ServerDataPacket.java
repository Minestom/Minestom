package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ServerDataPacket(@Nullable Component motd, @Nullable String iconBase64,
                               boolean previewsChat) implements ServerPacket {
    public static final ServerDataPacket PREVIEW_ENABLE = new ServerDataPacket(null, null, true);
    public static final ServerDataPacket PREVIEW_DISABLE = new ServerDataPacket(null, null, false);
    public ServerDataPacket(BinaryReader reader) {
        this(reader.readNullableComponent(), reader.readNullableSizedString(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeNullableComponent(motd);
        writer.writeNullableSizedString(iconBase64);
        writer.writeBoolean(previewsChat);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SERVER_DATA;
    }
}
