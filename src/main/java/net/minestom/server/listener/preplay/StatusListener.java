package net.minestom.server.listener.preplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.monitoring.EventsJFR;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ServerListPingType;

public final class StatusListener {

    public static void requestListener(StatusRequestPacket packet, PlayerConnection connection) {
        final ServerListPingType pingVersion = ServerListPingType.fromModernProtocolVersion(connection.getProtocolVersion());
        final ServerListPingEvent serverListPingEvent = new ServerListPingEvent(connection, pingVersion);
        EventDispatcher.callCancellable(serverListPingEvent, () ->
                connection.sendPacket(new ResponsePacket(pingVersion.getPingResponse(serverListPingEvent.getStatus()))));
        new EventsJFR.ServerPing(connection.getRemoteAddress().toString()).commit();
    }

    public static void pingRequestListener(ClientPingRequestPacket packet, PlayerConnection connection) {
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
