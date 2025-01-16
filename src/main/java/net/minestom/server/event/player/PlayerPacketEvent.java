package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record PlayerPacketEvent(@NotNull Player player, @NotNull ClientPacket packet, boolean cancelled) implements PlayerInstanceEvent, CancellableEvent<PlayerPacketEvent> {

    public PlayerPacketEvent(@NotNull Player player, @NotNull ClientPacket packet) {
        this(player, packet, false);
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<PlayerPacketEvent> {
        public Mutator(PlayerPacketEvent event) {
            super(event);
        }
        @Override
        public @NotNull PlayerPacketEvent mutated() {
            return new PlayerPacketEvent(this.event.player, this.event.packet, this.isCancelled());
        }
    }
}
