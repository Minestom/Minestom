package net.minestom.server.network.packet.client.common;

import net.kyori.adventure.resource.ResourcePackStatus;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientResourcePackStatusPacket(
        @NotNull UUID id,
        @NotNull ResourcePackStatus status
) implements ClientPacket {
    public ClientResourcePackStatusPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.UUID), readStatus(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.UUID, id);
        writer.writeEnum(ResourcePackStatus.class, status);
    }

    private static @NotNull ResourcePackStatus readStatus(@NotNull NetworkBuffer reader) {
        var ordinal = reader.read(NetworkBuffer.VAR_INT);
        return switch (ordinal) {
            case 0 -> ResourcePackStatus.SUCCESSFULLY_LOADED;
            case 1 -> ResourcePackStatus.DECLINED;
            case 2 -> ResourcePackStatus.FAILED_DOWNLOAD;
            case 3 -> ResourcePackStatus.ACCEPTED;
            case 4 -> ResourcePackStatus.DOWNLOADED;
            case 5 -> ResourcePackStatus.INVALID_URL;
            case 6 -> ResourcePackStatus.FAILED_RELOAD;
            case 7 -> ResourcePackStatus.DISCARDED;
            default -> throw new IllegalStateException("Unexpected resource pack status: " + ordinal);
        };
    }
}
