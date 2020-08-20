package net.minestom.server.network.packet.client;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.PlayerConnection;

public interface ClientPreplayPacket extends ClientPacket {

    ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    /**
     * Called when this packet is received
     *
     * @param connection the connection who sent the packet
     */
    void process(PlayerConnection connection);
}
