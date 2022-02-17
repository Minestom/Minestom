package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Listen to outgoing packets asynchronously.
 * <p>
 * Currently, do not support viewable packets.
 */
@ApiStatus.Experimental
public class PlayerPacketOutEvent implements PlayerEvent {

    private final Player player;
    private final ServerPacket packet;

    public PlayerPacketOutEvent(Player player, ServerPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull ServerPacket getPacket() {
        return packet;
    }
}
