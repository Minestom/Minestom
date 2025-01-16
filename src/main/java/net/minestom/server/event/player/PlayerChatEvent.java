package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Called every time a {@link Player} writes and sends something in the chat.
 * The event can be cancelled to not send anything, and the final message can be changed.
 */
public record PlayerChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                             @NotNull String rawMessage, @NotNull Component formattedMessage, boolean cancelled) implements PlayerInstanceEvent, CancellableEvent<PlayerChatEvent> {

    public PlayerChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                           @NotNull String rawMessage) {
        this(player, List.copyOf(recipients), rawMessage, buildDefaultChatMessage(player, rawMessage), false);
    }

    /**
     * Returns the players who will receive the message.
     * <p>
     * It can be modified to add and remove recipients.
     *
     * @return a unmodifiable collection of the message's targets
     */
    @Override
    public @NotNull Collection<Player> recipients() {
        return Collections.unmodifiableCollection(recipients);
    }

    /**
     * Gets the original message content sent by the player.
     *
     * @return the sender's message
     */
    @Override
    public @NotNull String rawMessage() {
        return rawMessage;
    }

    /**
     * Gets the final message component that will be sent.
     *
     * @return the chat message component
     */
    @Override
    public @NotNull Component formattedMessage() {
        return formattedMessage;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PlayerChatEvent> {
        private final Player player;
        private final Collection<Player> recipients;
        private final String rawMessage;

        private Component formattedMessage;
        private boolean cancelled;

        public Mutator(PlayerChatEvent event) {
            this.player = event.player;
            this.recipients = new ArrayList<>(event.recipients);
            this.rawMessage = event.rawMessage;
            this.formattedMessage = event.formattedMessage;
            this.cancelled = event.cancelled;
        }


        /**
         * Gets the final message component that will be sent.
         *
         * @return the chat message component
         */
        public @NotNull Component getFormattedMessage() {
            return formattedMessage;
        }

        /**
         * Used to change the final message component.
         *
         * @param message the new message component
         */
        public void setFormattedMessage(@NotNull Component message) {
            formattedMessage = message;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public @NotNull PlayerChatEvent mutated() {
            return new PlayerChatEvent(this.player, this.recipients, this.rawMessage, this.formattedMessage, this.cancelled);
        }
    }

    private static Component buildDefaultChatMessage(@NotNull Player player, @NotNull String rawMessage) {
        return Component.translatable("chat.type.text")
                .arguments(
                        Component.text(player.getUsername())
                                .insertion(player.getUsername())
                                .hoverEvent(player),
                        Component.text(rawMessage));
    }
}
