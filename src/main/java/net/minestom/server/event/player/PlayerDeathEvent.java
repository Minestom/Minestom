package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player die in {@link Player#kill()}.
 */
public class PlayerDeathEvent implements PlayerEvent {

    private final Player player;
    private Component deathText;
    private Component chatMessage;

    /**
     * @deprecated Use {@link #PlayerDeathEvent(Player, Component, Component)}
     */
    @Deprecated
    public PlayerDeathEvent(@NotNull Player player, JsonMessage deathText, JsonMessage chatMessage) {
        this(player, deathText.asComponent(), chatMessage.asComponent());
    }

    public PlayerDeathEvent(@NotNull Player player, Component deathText, Component chatMessage) {
        this.player = player;
        this.deathText = deathText;
        this.chatMessage = chatMessage;
    }

    /**
     * Gets the text displayed in the death screen.
     *
     * @return the death text, can be null
     * @deprecated Use {@link #getDeathText()}
     */
    @Nullable
    @Deprecated
    public JsonMessage getDeathTextJson() {
        return JsonMessage.fromComponent(deathText);
    }

    /**
     * Gets the text displayed in the death screen.
     *
     * @return the death text, can be null
     */
    @Nullable
    public Component getDeathText() {
        return deathText;
    }

    /**
     * Changes the text displayed in the death screen.
     *
     * @param deathText the death text to display, null to remove
     * @deprecated Use {@link #setDeathText(Component)}
     */
    @Deprecated
    public void setDeathText(@Nullable JsonMessage deathText) {
        this.deathText = deathText == null ? null : deathText.asComponent();
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
     * @deprecated Use {@link #getChatMessage()}
     */
    @Deprecated
    @Nullable
    public JsonMessage getChatMessageJson() {
        return JsonMessage.fromComponent(chatMessage);
    }

    /**
     * Gets the message sent to chat.
     *
     * @return the death chat message
     */
    @Nullable
    public Component getChatMessage() {
        return chatMessage;
    }

    /**
     * Changes the text sent in chat
     *
     * @param chatMessage the death message to send, null to remove
     * @deprecated Use {@link #setChatMessage(Component)}
     */
    @Deprecated
    public void setChatMessage(@Nullable JsonMessage chatMessage) {
        this.chatMessage = chatMessage == null ? null : chatMessage.asComponent();
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
