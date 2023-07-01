package net.minestom.server.network.packet.client.status;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.status.PongPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record PingPacket(long number) implements ClientPreplayPacket {
    public PingPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        final ClientPingServerEvent clientPingEvent = new ClientPingServerEvent(connection, number);
        EventDispatcher.call(clientPingEvent);

        if (clientPingEvent.isCancelled()) {
            connection.disconnect();
        } else {
            if (clientPingEvent.getDelay().isZero()) {
                connection.sendPacket(new PongPacket(clientPingEvent.getPayload()));
                connection.disconnect();
            } else {
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    connection.sendPacket(new PongPacket(clientPingEvent.getPayload()));
                    connection.disconnect();
                }).delay(clientPingEvent.getDelay()).schedule();
            }
        }
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, number);
    }
}
