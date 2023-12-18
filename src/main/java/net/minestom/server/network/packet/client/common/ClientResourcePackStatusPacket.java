package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.resourcepack.ResourcePackStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientResourcePackStatusPacket(
        @NotNull UUID id,
        @NotNull ResourcePackStatus status
) implements ClientPacket {
    public ClientResourcePackStatusPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.UUID), reader.readEnum(ResourcePackStatus.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.UUID, id);
        writer.writeEnum(ResourcePackStatus.class, status);
    }
}
