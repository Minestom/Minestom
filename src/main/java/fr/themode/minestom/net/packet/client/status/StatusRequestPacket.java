package fr.themode.minestom.net.packet.client.status;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.handshake.ResponsePacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.ping.ResponseData;
import fr.themode.minestom.ping.ResponseDataConsumer;

public class StatusRequestPacket implements ClientPreplayPacket {

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        ResponseDataConsumer consumer = MinecraftServer.getConnectionManager().getResponseDataConsumer();
        ResponseData responseData = new ResponseData();
        if (responseData == null)
            throw new NullPointerException("You need to register a ResponseDataConsumer");
        consumer.accept(connection, responseData);

        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.jsonResponse = responseData.build().toString();

        connection.sendPacket(responsePacket);
    }

    @Override
    public void read(PacketReader reader, Runnable callback) {
        // Empty
        callback.run();
    }
}
