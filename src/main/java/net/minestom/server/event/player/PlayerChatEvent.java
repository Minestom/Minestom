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
 * The event can be cancelled to do not send anything, and the message can be changed.
 */
public class PlayerChatEvent implements PlayerInstanceEvent, CancellableEvent {
    private final Player player;
    private final Collection<Player> recipients;
    private final String content;
    private Component finalMessage;

    private boolean cancelled;

    public PlayerChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                           @NotNull String content) {
        this.player = player;
        this.recipients = new ArrayList<>(recipients);
        this.content = content;
        this.finalMessage = buildDefaultChatMessage();
    }

    /**
     * Those are the players who will receive the message.
     * <p>
     * It can be modified to add or remove recipient.
     *
     * @return a modifiable list of message targets
     */
    public @NotNull Collection<Player> getRecipients() {
        return recipients;
    }

    /**
     * Gets the original message content sent by the player.
     *
     * @return the sender's message
     */
    public @NotNull String getContent() {
        return content;
    }

    /**
     * Gets the message format that will be sent.
     *
     * @return The chat message format.
     */
    public Component getFinalMessage() {
        return finalMessage;
    }

    /**
     * Used to change the message format.
     *
     * @param message the new message
     */
    public void setFinalMessage(@NotNull Component message) {
        this.finalMessage = message;
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
                                .clickEvent(ClickEvent.suggestCommand("/msg " + player.getUsername() + " "))
                                .hoverEvent(player),
                        Component.text(content));
    }
}
