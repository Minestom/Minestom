package net.minestom.server.network.packet.client.status;

import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.player.PlayerConnection;

public class LegacyServerListPingPacket implements ClientPreplayPacket {

    private byte payload;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {

    }

    @Override
    public void read(PacketReader reader) {
        this.payload = reader.readByte();
    }
}
