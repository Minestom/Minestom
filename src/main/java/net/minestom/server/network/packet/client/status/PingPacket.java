package net.minestom.server.network.packet.client.status;

import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.status.PongPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;

public class PingPacket implements ClientPreplayPacket {

    private long number;

    @Override
    public void process(PlayerConnection connection) {
        PongPacket pongPacket = new PongPacket(number);
        connection.sendPacket(pongPacket);
        connection.disconnect();
    }

    @Override
    public void read(BinaryReader reader) {
        this.number = reader.readLong();
    }
}
