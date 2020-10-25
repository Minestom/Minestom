package net.minestom.server.network.packet.client.handshake;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnect;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class HandshakePacket implements ClientPreplayPacket {

    /**
     * Text sent if a player tries to connect with an invalid version of the client
     */
    private static final ColoredText INVALID_VERSION_TEXT = ColoredText.of(ChatColor.RED, "Invalid Version, please use " + MinecraftServer.VERSION_NAME);

    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int nextState;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.protocolVersion = reader.readVarInt();
        this.serverAddress = reader.readSizedString();
        this.serverPort = reader.readUnsignedShort();
        this.nextState = reader.readVarInt();
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        switch (nextState) {
            case 1:
                connection.setConnectionState(ConnectionState.STATUS);
                break;
            case 2:
                if (protocolVersion == MinecraftServer.PROTOCOL_VERSION) {
                    connection.setConnectionState(ConnectionState.LOGIN);

                    if (connection instanceof NettyPlayerConnection) {
                        // Give to the connection the server info that the client used
                        ((NettyPlayerConnection) connection).refreshServerInformation(serverAddress, serverPort);
                    }
                } else {
                    // Incorrect client version
                    connection.sendPacket(new LoginDisconnect(INVALID_VERSION_TEXT.toString()));
                    connection.disconnect();
                }
                break;
            default:
                // Unexpected error
                break;
        }
    }
}
