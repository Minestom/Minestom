package net.minestom.server.network;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

/**
 * Called before packet processing
 */
@FunctionalInterface
public interface PacketPreProcessor {
    ClientPacket process(@NotNull ClientPacket packet, @NotNull PlayerConnection connection);
}