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

        // TODO: Skin
        //UUID adam = UUID.fromString("58ffa9d8-aee1-4587-8b79-41b754f6f238");
        //UUID mode = UUID.fromString("ab70ecb4-2346-4c14-a52d-7a091507c24e");
        UUID playerUuid = UUID.randomUUID();

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
