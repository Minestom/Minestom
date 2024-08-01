package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Called every time a {@link Player} writes and sends something in the chat.
 * The event can be cancelled to do not send anything, and the format can be changed.
 */
public class PlayerChatEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final Collection<Player> recipients;
    private String message;
    private Function<PlayerChatEvent, Component> chatFormat;

    private boolean cancelled;

    public PlayerChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                           @NotNull Function<PlayerChatEvent, Component> defaultChatFormat,
                           @NotNull String message) {
        this.player = player;
        this.recipients = new ArrayList<>(recipients);
        this.chatFormat = defaultChatFormat;
        this.message = message;
    }

    /**
     * Changes the chat format.
     *
     * @param chatFormat the custom chat format
     */
    public void setChatFormat(@NotNull Function<PlayerChatEvent, Component> chatFormat) {
        this.chatFormat = chatFormat;
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
     * Gets the message sent.
     *
     * @return the sender's message
     */
    public @NotNull String getMessage() {
        return message;
    }

    /**
     * Used to change the message.
     *
     * @param message the new message
     */
    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    /**
     * Used to retrieve the chat format for this message.
     * <p>
     *
     * @return the chat format which will be used
     */
    public @NotNull Function<@NotNull PlayerChatEvent, @NotNull Component> getChatFormatFunction() {
        return chatFormat;
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
}
