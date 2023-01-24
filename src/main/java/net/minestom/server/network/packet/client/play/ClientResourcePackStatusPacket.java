package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.resourcepack.ResourcePackStatus;
import org.jetbrains.annotations.NotNull;

public record ClientResourcePackStatusPacket(@NotNull ResourcePackStatus status) implements ClientPacket {
    public ClientResourcePackStatusPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(ResourcePackStatus.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(ResourcePackStatus.class, status);
    }
}
