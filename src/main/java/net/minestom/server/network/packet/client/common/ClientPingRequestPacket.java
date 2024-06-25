package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record ClientPingRequestPacket(long number) implements ClientPacket {
    public ClientPingRequestPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public boolean processImmediately() {
        return true;
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, number);
    }
}
