package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.event.AbstractPlayerChatEvent;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Called every time a {@link Player} write and send something in the chat.
 * The event can be cancelled to do not send anything, and the format can be changed.
 */
public class PlayerChatEvent implements AbstractPlayerChatEvent, CancellableEvent {

    private final Player player;
    private final Collection<Player> recipients;
    private final Supplier<Component> defaultChatFormat;
    private String message;
    private Function<AbstractPlayerChatEvent, Component> chatFormat;

    private boolean cancelled;

    public PlayerChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                           @NotNull Supplier<Component> defaultChatFormat,
                           @NotNull String message) {
        this.player = player;
        this.recipients = new ArrayList<>(recipients);
        this.defaultChatFormat = defaultChatFormat;
        this.message = message;
    }

    /**
     * Changes the chat format.
     *
     * @param chatFormat the custom chat format, null to use the default one
     * @deprecated Use {@link #setChatFormat(Function)}
     */
    @Deprecated
    public void setChatFormatJson(@Nullable Function<AbstractPlayerChatEvent, JsonMessage> chatFormat) {
        this.chatFormat = chatFormat == null ? null : chatFormat.andThen(JsonMessage::asComponent);
    }

    /**
     * Changes the chat format.
     *
     * @param chatFormat the custom chat format, null to use the default one
     */
    public void setChatFormat(@Nullable Function<AbstractPlayerChatEvent, Component> chatFormat) {
        this.chatFormat = chatFormat;
    }

    @Override
    public @NotNull Collection<Player> getRecipients() {
        return recipients;
    }

    @Override
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

    @Override
    public @Nullable Function<@NotNull AbstractPlayerChatEvent, @NotNull Component> getChatFormatFunction() {
        return chatFormat;
    }

    @Override
    public @NotNull Supplier<@NotNull Component> getDefaultChatFormat() {
        return defaultChatFormat;
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
