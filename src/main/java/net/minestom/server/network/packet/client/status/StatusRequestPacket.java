package net.minestom.server.network.packet.client.status;

import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.handshake.ResponsePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ServerListPingType;
import org.jetbrains.annotations.NotNull;

public record StatusRequestPacket() implements ClientPreplayPacket {
    public StatusRequestPacket(@NotNull NetworkBuffer reader) {
        this();
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        final ServerListPingType pingVersion = ServerListPingType.fromModernProtocolVersion(connection.getProtocolVersion());
        final ServerListPingEvent statusRequestEvent = new ServerListPingEvent(connection, pingVersion);
        EventDispatcher.callCancellable(statusRequestEvent, () ->
                connection.sendPacket(new ResponsePacket(pingVersion.getPingResponse(statusRequestEvent.getResponseData()))));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        // Empty
    }
}
