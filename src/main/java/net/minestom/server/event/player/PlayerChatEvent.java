package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Called every time a {@link Player} writes and sends something in the chat.
 * The event can be cancelled to not send anything, and the final message can be changed.
 */
public class PlayerChatEvent implements PlayerInstanceEvent, CancellableEvent {
    private final Player player;
    private final Collection<Player> recipients;
    private final String rawMessage;
    private Component formattedMessage;
    private boolean cancelled;

    public PlayerChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                           @NotNull String rawMessage) {
        this.player = player;
        this.recipients = new ArrayList<>(recipients);
        this.rawMessage = rawMessage;
        formattedMessage = buildDefaultChatMessage();
    }

    /**
     * Returns the players who will receive the message.
     * <p>
     * It can be modified to add and remove recipients.
     *
     * @return a modifiable list of the message's targets
     */
    public @NotNull Collection<Player> getRecipients() {
        return recipients;
    }

    /**
     * Gets the original message content sent by the player.
     *
     * @return the sender's message
     */
    public @NotNull String getRawMessage() {
        return rawMessage;
    }

    /**
     * Gets the final message component that will be sent.
     *
     * @return the chat message component
     */
    public Component getFormattedMessage() {
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
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    private Component buildDefaultChatMessage() {
        return Component.translatable("chat.type.text")
                .arguments(
                        Component.text(player.getUsername())
                                .insertion(player.getUsername())
                                .hoverEvent(player),
                        Component.text(rawMessage));
    }
}
