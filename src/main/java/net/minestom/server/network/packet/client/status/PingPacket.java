package net.minestom.server.network.packet.client.status;

import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.status.PongPacket;
import net.minestom.server.network.player.PlayerConnection;

public class PingPacket implements ClientPreplayPacket {

    private long number;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        PongPacket pongPacket = new PongPacket(number);
        connection.sendPacket(pongPacket);
        connection.disconnect();
    }

    @Override
    public void read(PacketReader reader) {
        this.number = reader.readLong();
    }
}
