package net.minestom.server.weather.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.netty.packet.FramedPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A weather manager for a player.
 */
public class PlayerWeatherManager extends ForwardingWeatherManager {
    private final Player player;

    /**
     * Creates a new player weather manager.
     *
     * @param player the player
     */
    public PlayerWeatherManager(@NotNull Player player) {
        super(player::getInstance);
        this.player = player;
    }

    @Override
    protected void sendWeatherPackets(@NotNull Collection<FramedPacket> packets) {
        if (!packets.isEmpty() && this.player.getPlayerConnection() instanceof NettyPlayerConnection) {
            final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) this.player.getPlayerConnection();

            for (FramedPacket packet : packets) {
                nettyPlayerConnection.write(packet, true);
            }
        }
    }
}
