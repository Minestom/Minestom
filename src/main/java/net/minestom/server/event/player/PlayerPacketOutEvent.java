package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Listen to outgoing packets asynchronously.
 * <p>
 * Currently, do not support viewable packets.
 */
@ApiStatus.Experimental
public class PlayerPacketOutEvent implements PlayerEvent {

    private final Player player;
    private final ServerPacket packet;

    private boolean discarded = false;
    private final List<SendablePacket> appendix = new ArrayList<>();

    public PlayerPacketOutEvent(@NotNull Player player, @NotNull ServerPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * @return The current packet.
     */
    public @NotNull ServerPacket getPacket() {
        return packet;
    }

    /**
     * Discards the original packet.
     *
     * @return The event instance.
     */
    @NotNull
    public PlayerPacketOutEvent discardOriginal() {
        discarded = true;
        return this;
    }

    /**
     * Appends a packet to the appendix which will be sent additionally to the original packet.
     *
     * @param packet The packet to add to the appendix.
     * @return The event instance.
     */
    @NotNull
    public PlayerPacketOutEvent append(@NotNull SendablePacket packet) {
        appendix.add(packet);
        return this;
    }

    /**
     * Discards the original packet and appends the given packet.
     *
     * @param packet The replacement packet.
     */
    public void replace(@NotNull SendablePacket packet) {
        discardOriginal().append(packet);
    }

    /**
     * Iterates through all given packets.
     *
     * @param consumer The consumer which consumes all packets.
     */
    public void iterateAppendix(@NotNull Consumer<SendablePacket> consumer) {
        if (!isDiscarded()) consumer.accept(packet);
        appendix.forEach(consumer);
    }

    /**
     * @return true if the packet was discarded.
     */
    public boolean isDiscarded() {
        return discarded;
    }
}
