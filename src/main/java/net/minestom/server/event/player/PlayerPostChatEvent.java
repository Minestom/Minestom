package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.AbstractPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Called after a {@link Player} write and send something in the chat.
 * Nothing about this event can be changed, if you're looking for changing something about the chat message or cancelling it, listen for {@link PlayerChatEvent}.
 */
public class PlayerPostChatEvent implements AbstractPlayerChatEvent {

    private final Player player;
    private final Collection<Player> recipients;
    private final Supplier<Component> defaultChatFormat;
    private final String message;
    private final Function<AbstractPlayerChatEvent, Component> chatFormat;

    public PlayerPostChatEvent(@NotNull Player player, @NotNull Collection<Player> recipients,
                               @NotNull Supplier<Component> defaultChatFormat,
                               @NotNull String message, Function<AbstractPlayerChatEvent, Component> chatFormat) {
        this.player = player;
        this.recipients = new ArrayList<>(recipients);
        this.defaultChatFormat = defaultChatFormat;
        this.message = message;
        this.chatFormat = chatFormat;
    }

    public PlayerPostChatEvent(PlayerChatEvent playerChatEvent) {
        this(playerChatEvent.getPlayer(), playerChatEvent.getRecipients(), playerChatEvent.getDefaultChatFormat(), playerChatEvent.getMessage(), playerChatEvent.getChatFormatFunction());
    }

    @Override
    public @NotNull Collection<Player> getRecipients() {
        return recipients;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public @Nullable Function<@NotNull AbstractPlayerChatEvent, @NotNull Component> getChatFormatFunction() {
        return chatFormat;
    }

    public @NotNull Supplier<@NotNull Component> getDefaultChatFormat() {
        return defaultChatFormat;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
