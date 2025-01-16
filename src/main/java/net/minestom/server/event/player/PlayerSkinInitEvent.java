package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called at the player connection to initialize his skin.
 */
public record PlayerSkinInitEvent(@NotNull Player player, @Nullable PlayerSkin skin) implements PlayerEvent, MutableEvent<PlayerSkinInitEvent> {

    /**
     * Gets the spawning skin of the player.
     *
     * @return the player skin, or null if not any
     */
    @Override
    public @Nullable PlayerSkin skin() {
        return skin;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<PlayerSkinInitEvent> {
        private final Player player;
        private PlayerSkin skin;

        public Mutator(PlayerSkinInitEvent event) {
            this.player = event.player;
            this.skin = event.skin;
        }

        /**
         * Gets the spawning skin of the player.
         *
         * @return the player skin, or null if not any
         */
        public @Nullable PlayerSkin getSkin() {
            return skin;
        }

        /**
         * Sets the spawning skin of the player.
         *
         * @param skin the new player skin
         */
        public void setSkin(@Nullable PlayerSkin skin) {
            this.skin = skin;
        }

        @Override
        public @NotNull PlayerSkinInitEvent mutated() {
            return new PlayerSkinInitEvent(this.player, this.skin);
        }
    }
}
