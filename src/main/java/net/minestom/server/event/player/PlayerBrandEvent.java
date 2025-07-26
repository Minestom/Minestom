package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player sends a {@link net.minestom.server.network.packet.server.common.PluginMessagePacket} with the {@code minecraft:brand} channel.
 */
public class PlayerBrandEvent implements PlayerInstanceEvent {
    private final Player player;
    private final String brand;

    public PlayerBrandEvent(@NotNull Player player, @NotNull String brand) {
        this.player = player;
        this.brand = brand;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Returns the brand name of the player's client.
     * @return the brand
     */
    public @NotNull String getBrand() {
        return brand;
    }
}
