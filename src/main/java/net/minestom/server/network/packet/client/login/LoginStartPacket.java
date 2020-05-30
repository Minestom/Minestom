package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.player.PlayerConnection;

import java.util.UUID;

public class LoginStartPacket implements ClientPreplayPacket {

    public String username;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        // TODO send encryption request OR directly login success

        UUID playerUuid = connectionManager.getPlayerConnectionUuid(connection);

        LoginSuccessPacket successPacket = new LoginSuccessPacket(playerUuid, username);
        connection.sendPacket(successPacket);

        connection.setConnectionState(ConnectionState.PLAY);
        connectionManager.createPlayer(playerUuid, username, connection);
    }

    @Override
    public void read(PacketReader reader) {
        this.username = reader.readSizedString();
    }
}
