package net.minestom.server.network.packet.client;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import org.jetbrains.annotations.NotNull;

public abstract class ClientPlayPacket implements ClientPacket {
    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();

    /**
     * Processes the packet for {@code player}.
     * <p>
     * Called during the player tick and forwarded to the {@link PacketListenerManager}.
     *
     * @param player the player who sent the packet
     */
    public void process(@NotNull Player player) {
        PACKET_LISTENER_MANAGER.processClientPacket(this, player);
    }
}
