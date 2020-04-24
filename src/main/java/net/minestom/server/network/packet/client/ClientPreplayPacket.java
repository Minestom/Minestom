package net.minestom.server.network.packet.client;

import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.PlayerConnection;

public interface ClientPreplayPacket extends ClientPacket {


    void process(PlayerConnection connection, ConnectionManager connectionManager);

}
