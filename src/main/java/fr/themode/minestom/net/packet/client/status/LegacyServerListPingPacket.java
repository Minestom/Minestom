package fr.themode.minestom.net.packet.client.status;

import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.player.PlayerConnection;

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
