package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Listen to outgoing packets asynchronously.
 * <p>
 * Currently, do not support viewable packets.
 */
@ApiStatus.Experimental
public record PlayerPacketOutEvent(@NotNull Player player, @NotNull ServerPacket packet, boolean cancelled) implements PlayerEvent, CancellableEvent<PlayerPacketOutEvent> {

    public PlayerPacketOutEvent(@NotNull Player player, @NotNull ServerPacket packet) {
        this(player, packet, false);
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<PlayerPacketOutEvent> {
        public Mutator(@NotNull PlayerPacketOutEvent event) {
            super(event);
        }

        @Override
        public @NotNull PlayerPacketOutEvent mutated() {
            return new PlayerPacketOutEvent(this.event.player, this.event.packet, this.isCancelled());
        }
    }
}
