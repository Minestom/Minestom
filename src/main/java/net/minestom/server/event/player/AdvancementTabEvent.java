package net.minestom.server.event.player;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} opens the advancement screens or switch the tab
 * and when he closes the screen.
 */
public class AdvancementTabEvent extends Event {

    private final Player player;
    private final AdvancementAction action;
    private final String tabId;

    public AdvancementTabEvent(@NotNull Player player, @NotNull AdvancementAction action, @NotNull String tabId) {
        this.player = player;
        this.action = action;
        this.tabId = tabId;
    }

    /**
     * Gets the {@link Player} responsible for the event.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    @NotNull
    public AdvancementAction getAction() {
        return action;
    }

    /**
     * Gets the tab id.
     * <p>
     * Not null ony if {@link #getAction()} is equal to {@link AdvancementAction#OPENED_TAB}.
     *
     * @return the tab id
     */
    @NotNull
    public String getTabId() {
        return tabId;
    }
}
