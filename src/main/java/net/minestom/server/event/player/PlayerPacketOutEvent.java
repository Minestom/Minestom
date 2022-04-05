package net.minestom.server.event.player;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
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
 * Currently, this does not fully support viewable packets. For more information see {@link net.minestom.server.utils.PacketUtils#prepareViewablePacket(Viewable, ServerPacket, Entity) packet controlling}.
 */
@ApiStatus.Experimental
public class PlayerPacketOutEvent implements PlayerEvent {

    private final Player player;
    private final ServerPacket packet;
    private final List<SendablePacket> appendix = new ArrayList<>();
    private boolean discarded = false;

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
    public PlayerPacketOutEvent discardPacket() {
        discarded = true;
        return this;
    }

    /**
     * Appends a packet to the appendix which will be sent additionally to the original packet if it has not been discarded.
     *
     * @param packet The packet to add to the appendix.
     * @return The event instance.
     */
    @NotNull
    public PlayerPacketOutEvent append(@NotNull ServerPacket packet) {
        appendix.add(packet);
        return this;
    }

    /**
     * Discards the packet and appends the given packet to the appendix.
     *
     * @param packet The replacement packet.
     */
    public void replace(@NotNull ServerPacket packet) {
        discardPacket().append(packet);
    }

    /**
     * @return The original appendix instance to allow later modifications from extensions.
     */
    @NotNull
    public List<SendablePacket> getAppendix() {
        return appendix;
    }

    /**
     * @param consumer The consumer which consumes all packets.
     */
    public void usePackets(@NotNull Consumer<SendablePacket> consumer) {
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
