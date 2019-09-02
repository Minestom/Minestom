package fr.themode.minestom.net.packet.client.status;

import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.status.PongPacket;
import fr.themode.minestom.net.player.PlayerConnection;

public class PingPacket implements ClientPreplayPacket {

    private long number;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        PongPacket pongPacket = new PongPacket(number);
        connection.sendPacket(pongPacket);
        connection.getClient().close();
    }

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readLong(value -> {
            number = value;
            callback.run();
        });
    }
}
