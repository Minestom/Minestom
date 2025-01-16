package net.minestom.server.event.player;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} opens the advancement screens or switch the tab
 * and when he closes the screen.
 */
public record AdvancementTabEvent(@NotNull Player player, @NotNull AdvancementAction action, @NotNull String tabId) implements PlayerInstanceEvent {

    /**
     * Gets the action.
     *
     * @return the action
     */
    @Override
    public @NotNull AdvancementAction action() {
        return action;
    }

    /**
     * Gets the tab id.
     * <p>
     * Not null ony if {@link #action ()} is equal to {@link AdvancementAction#OPENED_TAB}.
     *
     * @return the tab id
     */
    @Override
    public @NotNull String tabId() {
        return tabId;
    }
}
