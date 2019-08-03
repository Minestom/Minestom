package fr.themode.minestom.net.packet.client.status;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.status.PongPacket;
import fr.themode.minestom.net.player.PlayerConnection;

public class PingPacket implements ClientPreplayPacket {

    private long number;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        PongPacket pongPacket = new PongPacket(number);
        connection.sendPacket(pongPacket);
        connection.getConnection().close();
    }

    @Override
    public void read(Buffer buffer) {
        this.number = buffer.getLong();
    }
}
