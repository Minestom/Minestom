package fr.themode.minestom.net.packet.client.handshake;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import static fr.themode.minestom.utils.Utils.readString;
import static fr.themode.minestom.utils.Utils.readVarInt;

public class HandshakePacket implements ClientPreplayPacket {

    private int nextState;

    @Override
    public void read(Buffer buffer) {
        int protocolVersion = readVarInt(buffer);
        String serverAddress = readString(buffer);
        short serverPort = buffer.getShort();
        this.nextState = readVarInt(buffer);
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
