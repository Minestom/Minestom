package net.minestom.server.network.packet.client.status;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.handshake.ResponsePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.ping.PingResponse.*;

public class StatusRequestPacket implements ClientPreplayPacket {

    @SuppressWarnings("deprecation") // we need to continue handling the ResponseDataConsumer until it's removed
    @Override
    public void process(@NotNull PlayerConnection connection) {
        ResponseDataConsumer consumer = MinecraftServer.getResponseDataConsumer();
        ResponseData responseData = new ResponseData();

        // Fill default params
        responseData.setVersion(MinecraftServer.VERSION_NAME);
        responseData.setProtocol(MinecraftServer.PROTOCOL_VERSION);
        responseData.setMaxPlayer(0);
        responseData.setOnline(0);
        responseData.setDescription(Component.text("Minestom Server"));
        responseData.setFavicon("");

        if (consumer != null) {
            consumer.accept(connection, responseData);
        }

        // Call event
        ServerListPingEvent statusRequestEvent = new ServerListPingEvent(responseData, connection);
        MinecraftServer.getGlobalEventHandler().callCancellableEvent(ServerListPingEvent.class, statusRequestEvent,
                () -> {
                    final ResponsePacket responsePacket = new ResponsePacket();

                    // check if we need to use a legacy response
                    if (connection.getProtocolVersion() >= 713) {
                        responsePacket.jsonResponse = FULL_RGB.getResponse(statusRequestEvent.getResponseData()).toString();
                    } else {
                        responsePacket.jsonResponse = NAMED_COLORS.getResponse(statusRequestEvent.getResponseData()).toString();
                    }

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
