package fr.themode.minestom.net.packet.client.status;

import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.handshake.ResponsePacket;
import fr.themode.minestom.net.player.PlayerConnection;

public class StatusRequestPacket implements ClientPreplayPacket {

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        ResponsePacket responsePacket = new ResponsePacket();
        connection.sendPacket(responsePacket);
    }

    @Override
    public void read(PacketReader reader) {
        // Empty
    }
}
