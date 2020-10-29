package net.minestom.server.event.player;

import net.minestom.server.chat.RichMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

/**
 * Called every time a {@link Player} write and send something in the chat.
 * The event can be cancelled to do not send anything, and the format can be changed.
 */
public class PlayerChatEvent extends CancellableEvent {

    private final Player sender;
    private final Collection<Player> recipients;
    private String message;
    private Function<PlayerChatEvent, RichMessage> chatFormat;

    public PlayerChatEvent(@NotNull Player sender, @NotNull Collection<Player> recipients, @NotNull String message) {
        this.sender = sender;
        this.recipients = new ArrayList<>(recipients);
        this.message = message;
    }

    /**
     * Changes the chat format.
     *
     * @param chatFormat the custom chat format, null to use the default one
     */
    public void setChatFormat(@Nullable Function<PlayerChatEvent, RichMessage> chatFormat) {
        this.chatFormat = chatFormat;
    }

    /**
     * Gets the message sender.
     *
     * @return the sender
     */
    @NotNull
    public Player getSender() {
        return sender;
    }

    /**
     * Those are the players who will receive the message.
     * <p>
     * It can be modified to add or remove recipient.
     *
     * @return a modifiable list of message targets
     */
    @NotNull
    public Collection<Player> getRecipients() {
        return recipients;
    }

    /**
     * Gets the message sent.
     *
     * @return the sender's message
     */
    @NotNull
    public String getMessage() {
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
     * If null, the default format will be used.
     *
     * @return the chat format which will be used, null if this is the default one
     */
    @Nullable
    public Function<PlayerChatEvent, RichMessage> getChatFormatFunction() {
        return chatFormat;
    }
}
