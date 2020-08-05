package net.minestom.server.event.player;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called when the players open the advancement screens or switch the tab
 * and when he closes the screen
 */
public class AdvancementTabEvent extends Event {

    private final Player player;
    private final AdvancementAction action;
    private final String tabId;

    public AdvancementTabEvent(Player player, AdvancementAction action, String tabId) {
        this.player = player;
        this.action = action;
        this.tabId = tabId;
    }

    /**
     * Get the player responsive for the event
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the action
     *
     * @return the action
     */
    public AdvancementAction getAction() {
        return action;
    }

    /**
     * Get the tab id
     * <p>
     * Not null ony if {@link #getAction()} is equal to {@link AdvancementAction#OPENED_TAB}
     *
     * @return the tab id
     */
    public String getTabId() {
        return tabId;
    }
}
