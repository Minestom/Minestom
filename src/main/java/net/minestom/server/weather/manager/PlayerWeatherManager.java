package net.minestom.server.weather.manager;

import java.util.Collection;
import java.util.Collections;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.NotNull;

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
        super(Collections::emptyList);
        this.player = player;
    }

    @Override
    protected void sendWeatherPackets(@NotNull Collection<SendablePacket> packets) {
        if (!packets.isEmpty() && this.player.getPlayerConnection() instanceof final PlayerSocketConnection psc) {
            for (SendablePacket packet : packets) {
                psc.sendPacket(packet);
            }
        }
    }
}
