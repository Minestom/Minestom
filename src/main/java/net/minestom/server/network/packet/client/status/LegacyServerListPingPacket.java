package net.minestom.server.network.packet.client.status;

import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;

public class LegacyServerListPingPacket implements ClientPreplayPacket {

    private byte payload;

    @Override
    public void process(PlayerConnection connection) {

    }

    @Override
    public void read(BinaryReader reader) {
        this.payload = reader.readByte();
    }
}
