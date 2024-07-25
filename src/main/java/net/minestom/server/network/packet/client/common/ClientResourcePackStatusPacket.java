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
    public static NetworkBuffer.Type<ClientResourcePackStatusPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ClientResourcePackStatusPacket value) {
            buffer.write(NetworkBuffer.UUID, value.id);
            buffer.write(NetworkBuffer.VAR_INT, statusId(value.status));
        }

        @Override
        public ClientResourcePackStatusPacket read(@NotNull NetworkBuffer buffer) {
            return new ClientResourcePackStatusPacket(buffer.read(NetworkBuffer.UUID), readStatus(buffer));
        }
    };

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

    private static int statusId(@NotNull ResourcePackStatus status) {
        return switch (status) {
            case SUCCESSFULLY_LOADED -> 0;
            case DECLINED -> 1;
            case FAILED_DOWNLOAD -> 2;
            case ACCEPTED -> 3;
            case DOWNLOADED -> 4;
            case INVALID_URL -> 5;
            case FAILED_RELOAD -> 6;
            case DISCARDED -> 7;
        };
    }
}
