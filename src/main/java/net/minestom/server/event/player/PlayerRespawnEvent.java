package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.trait.mutation.EventMutator;
import org.jetbrains.annotations.NotNull;

/**
 * Called when {@link Player#respawn()} is executed (for custom respawn or as a result of
 * {@link net.minestom.server.network.packet.client.play.ClientStatusPacket}
 */
public record PlayerRespawnEvent(@NotNull Player player, @NotNull Pos respawnPosition) implements PlayerEvent, MutableEvent<PlayerRespawnEvent> {

    public PlayerRespawnEvent(@NotNull Player player) {
        this(player, player.getRespawnPoint());
    }

    /**
     * Gets the respawn position.
     * <p>
     * Is by default {@link Player#getRespawnPoint()}
     *
     * @return the respawn position
     */
    @Override
    public @NotNull Pos respawnPosition() {
        return respawnPosition;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<PlayerRespawnEvent> {
        private final Player player;
        private Pos respawnPosition;

        public Mutator(PlayerRespawnEvent event) {
            this.player = event.player;
            this.respawnPosition = event.respawnPosition;
        }

        /**
         * Gets the respawn position.
         * <p>
         * Is by default {@link Player#getRespawnPoint()}
         *
         * @return the respawn position
         */
        public @NotNull Pos getRespawnPosition() {
            return respawnPosition;
        }

        /**
         * Changes the respawn position.
         *
         * @param respawnPosition the new respawn position
         */
        public void setRespawnPosition(@NotNull Pos respawnPosition) {
            this.respawnPosition = respawnPosition;
        }

        @Override
        public @NotNull PlayerRespawnEvent mutated() {
            return new PlayerRespawnEvent(this.player, this.respawnPosition);
        }
    }
}
