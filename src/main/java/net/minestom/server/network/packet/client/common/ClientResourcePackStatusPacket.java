package net.minestom.server.network.packet.client.common;

import net.kyori.adventure.resource.ResourcePackStatus;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.UUID;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientResourcePackStatusPacket(
        @NotNull UUID id,
        @NotNull ResourcePackStatus status
) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientResourcePackStatusPacket> SERIALIZER = NetworkBufferTemplate.template(
            UUID, ClientResourcePackStatusPacket::id,
            VAR_INT.transform(ClientResourcePackStatusPacket::readStatus, ClientResourcePackStatusPacket::statusId), ClientResourcePackStatusPacket::status,
            ClientResourcePackStatusPacket::new
    );

    private static @NotNull ResourcePackStatus readStatus(int id) {
        return switch (id) {
            case 0 -> ResourcePackStatus.SUCCESSFULLY_LOADED;
            case 1 -> ResourcePackStatus.DECLINED;
            case 2 -> ResourcePackStatus.FAILED_DOWNLOAD;
            case 3 -> ResourcePackStatus.ACCEPTED;
            case 4 -> ResourcePackStatus.DOWNLOADED;
            case 5 -> ResourcePackStatus.INVALID_URL;
            case 6 -> ResourcePackStatus.FAILED_RELOAD;
            case 7 -> ResourcePackStatus.DISCARDED;
            default -> throw new IllegalStateException("Unexpected resource pack status: " + id);
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
