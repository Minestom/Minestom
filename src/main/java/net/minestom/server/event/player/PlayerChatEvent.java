package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.message.MessageSender;
import net.minestom.server.message.registry.CommonChatType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

/**
 * Called every time a {@link Player} write and send something in the chat.
 * The event can be cancelled to do not send anything, and the format can be changed.
 */
public class PlayerChatEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final Collection<Player> recipients;
    private final String rawMessage;
    private Function<PlayerChatEvent, Component> chatFormat;

    private boolean cancelled;
    private MessageSignature signature;
    private MessageSender sender;
    private final Component message;
    private int chatType;

    public PlayerChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                           @NotNull String rawMessage, @NotNull MessageSignature signature,
                           @NotNull MessageSender sender, @NotNull Component message) {
        this.player = player;
        this.recipients = new ArrayList<>(recipients);
        this.rawMessage = rawMessage;
        this.message = message;
        this.signature = signature;
        this.sender = sender;
        this.chatType = CommonChatType.CHAT.getId();
    }

    public MessageSender getSender() {
        return sender;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    /**
     * Changes the chat format.
     *
     * @param chatFormat the custom chat format, null to use the default one
     */
    public void setChatFormat(@Nullable Function<PlayerChatEvent, Component> chatFormat) {
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
    public @NotNull String getRawMessage() {
        return rawMessage;
    }

    public @NotNull Component getMessage() {
        return message;
    }

    /**
     * Used to retrieve the chat format for this message.
     * <p>
     * If null, the default format will be used.
     *
     * @return the chat format which will be used, null if this is the default one
     */
    public @Nullable Function<@NotNull PlayerChatEvent, @NotNull Component> getChatFormatFunction() {
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

    public MessageSignature getSignature() {
        return signature;
    }

    public void setSignature(@NotNull MessageSignature signature) {
        this.signature = signature;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }
}
