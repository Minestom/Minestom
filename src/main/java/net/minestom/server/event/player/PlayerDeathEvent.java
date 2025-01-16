package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player die in {@link Player#kill()}.
 */
public record PlayerDeathEvent(@NotNull Player player, @Nullable Component deathText, @Nullable Component chatMessage) implements PlayerInstanceEvent, MutableEvent<PlayerDeathEvent> {

    /**
     * Gets the text displayed in the death screen.
     *
     * @return the death text, can be null
     */
    @Override
    public @Nullable Component deathText() {
        return deathText;
    }

    /**
     * Gets the message sent to chat.
     *
     * @return the death chat message, can be null
     */
    @Override
    public @Nullable Component chatMessage() {
        return chatMessage;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<PlayerDeathEvent> {
        private final Player player;
        private Component deathText;
        private Component chatMessage;

        public Mutator(PlayerDeathEvent event) {
            this.player = event.player;
            this.deathText = event.deathText;
            this.chatMessage = event.chatMessage;
        }

        /**
         * Gets the text displayed in the death screen.
         *
         * @return the death text, can be null
         */
        public @Nullable Component getDeathText() {
            return deathText;
        }

        /**
         * Changes the text displayed in the death screen.
         *
         * @param deathText the death text to display, null to remove
         */
        public void setDeathText(@Nullable Component deathText) {
            this.deathText = deathText;
        }

        /**
         * Gets the message sent to chat.
         *
         * @return the death chat message
         */
        public @Nullable Component getChatMessage() {
            return chatMessage;
        }

        /**
         * Changes the text sent in chat
         *
         * @param chatMessage the death message to send, null to remove
         */
        public void setChatMessage(@Nullable Component chatMessage) {
            this.chatMessage = chatMessage;
        }

        @Override
        public @NotNull PlayerDeathEvent mutated() {
            return new PlayerDeathEvent(this.player, this.deathText, this.chatMessage);
        }
    }
}
