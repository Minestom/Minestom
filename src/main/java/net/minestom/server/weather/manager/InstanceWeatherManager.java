package net.minestom.server.weather.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.netty.packet.FramedPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A weather manager for instances.
 */
public class InstanceWeatherManager extends ForwardingWeatherManager {
    private final Instance instance;

    /**
     * Creates a new instance weather manager.
     */
    public InstanceWeatherManager(@NotNull Instance instance) {
        super(MinecraftServer.getGlobalWeatherManager());
        this.instance = instance;
    }

    @Override
    protected void sendWeatherPackets(@NotNull Collection<FramedPacket> packets) {
        if (!packets.isEmpty()) {
            for (Player player : this.instance.getPlayers()) {
                player.getWeatherManager().sendWeatherPackets(packets);
            }
        }
    }
}
