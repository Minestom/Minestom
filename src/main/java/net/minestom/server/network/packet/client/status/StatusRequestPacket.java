package net.minestom.server.network.packet.client.status;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.handshake.ResponsePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ServerListPingType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class StatusRequestPacket implements ClientPreplayPacket {

    @Override
    public void process(@NotNull PlayerConnection connection) {
        final ServerListPingType pingVersion = ServerListPingType.fromModernProtocolVersion(connection.getProtocolVersion());
        final ServerListPingEvent statusRequestEvent = new ServerListPingEvent(connection, pingVersion);
        MinecraftServer.getGlobalEventHandler().callCancellableEvent(ServerListPingEvent.class, statusRequestEvent, () -> {
            final ResponsePacket responsePacket = new ResponsePacket();
            responsePacket.jsonResponse = pingVersion.getPingResponse(statusRequestEvent.getResponseData());

            connection.sendPacket(responsePacket);
        });
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        // Empty
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        // Empty
    }
}
