package net.minestom.server.network.packet.client;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

public interface ClientPreplayPacket extends ClientPacket {

    ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    /**
     * Called when the packet is received.
     *
     * @param connection the connection who sent the packet
     */
    void process(@NotNull PlayerConnection connection);
}
