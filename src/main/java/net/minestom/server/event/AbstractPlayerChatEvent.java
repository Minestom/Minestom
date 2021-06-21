package net.minestom.server.event;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public interface AbstractPlayerChatEvent extends PlayerEvent {

    /**
     * Those are the players who will receive the message.
     * <p>
     * It can be modified to add or remove recipient.
     *
     * @return a modifiable list of message targets
     */
    @NotNull Collection<Player> getRecipients();

    /**
     * Gets the message sent.
     *
     * @return the sender's message
     */
    @NotNull String getMessage();

    /**
     * Used to retrieve the chat format for this message.
     * <p>
     * If null, the default format will be used.
     *
     * @return the chat format which will be used, null if this is the default one
     */
    @Nullable Function<@NotNull AbstractPlayerChatEvent, @NotNull Component> getChatFormatFunction();

    @NotNull Supplier<@NotNull Component> getDefaultChatFormat();
}