package net.minestom.server.listener.preplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ServerListPingType;
import org.jetbrains.annotations.NotNull;

public final class StatusListener {

    public static void requestListener(@NotNull StatusRequestPacket packet, @NotNull PlayerConnection connection) {
        final ServerListPingType pingVersion = ServerListPingType.fromModernProtocolVersion(connection.getProtocolVersion());
        final ServerListPingEvent statusRequestEvent = new ServerListPingEvent(connection, pingVersion);
        EventDispatcher.callCancellable(statusRequestEvent, () ->
                connection.sendPacket(new ResponsePacket(pingVersion.getPingResponse(statusRequestEvent.getResponseData()))));
    }

    public static void pingRequestListener(@NotNull ClientPingRequestPacket packet, @NotNull PlayerConnection connection) {
        final ClientPingServerEvent clientPingEvent = new ClientPingServerEvent(connection, packet.number());
        EventDispatcher.call(clientPingEvent);

        if (clientPingEvent.isCancelled()) {
            connection.disconnect();
        } else {
            if (clientPingEvent.getDelay().isZero()) {
                connection.sendPacket(new PingResponsePacket(clientPingEvent.getPayload()));
                connection.disconnect();
            } else {
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    connection.sendPacket(new PingResponsePacket(clientPingEvent.getPayload()));
                    connection.disconnect();
                }).delay(clientPingEvent.getDelay()).schedule();
            }
        }
    }

}
