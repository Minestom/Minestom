package fr.themode.minestom.net.packet.client.handshake;

import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.player.PlayerConnection;

public class HandshakePacket implements ClientPreplayPacket {

    private int nextState;

    @Override
    public void read(PacketReader reader) {
        int protocolVersion = reader.readVarInt();
        String serverAddress = reader.readSizedString();
        short serverPort = reader.readShort();
        this.nextState = reader.readVarInt();
    }

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        switch (nextState) {
            case 1:
                connection.setConnectionState(ConnectionState.STATUS);
                break;
            case 2:
                connection.setConnectionState(ConnectionState.LOGIN);
                break;
            default:
                // Unexpected error
                break;
        }
    }
}
