package fr.themode.minestom.net.packet.client.handshake;

import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.player.PlayerConnection;

public class HandshakePacket implements ClientPreplayPacket {

    private int protocolVersion;
    private String serverAddress;
    private short serverPort;
    private int nextState;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(value -> protocolVersion = value);
        reader.readSizedString(s -> serverAddress = s);
        reader.readShort(value -> serverPort = value);
        reader.readVarInt(value -> {
            nextState = value;
            callback.run();
        });
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
