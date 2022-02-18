package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player die in {@link Player#kill()}.
 */
public class PlayerDeathEvent implements PlayerEvent, EntityInstanceEvent {

    private final Player player;
    private Component deathText;
    private Component chatMessage;

    public PlayerDeathEvent(@NotNull Player player, Component deathText, Component chatMessage) {
        this.player = player;
        this.deathText = deathText;
        this.chatMessage = chatMessage;
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
    public @NotNull Player getPlayer() {
        return player;
    }
}
