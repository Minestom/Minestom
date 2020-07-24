package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;

/**
 * Called at the player connection to initialize his skin
 */
public class PlayerSkinInitEvent extends Event {

    private final Player player;
    private PlayerSkin skin;

    public PlayerSkinInitEvent(Player player) {
        this.player = player;
    }

    /**
     * Get the player whose the skin is getting initialized
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the spawning skin of the player
     *
     * @return the player skin, or null if not any
     */
    public PlayerSkin getSkin() {
        return skin;
    }

    /**
     * Set the spawning skin of the player
     *
     * @param skin the new player skin
     */
    public void setSkin(PlayerSkin skin) {
        this.skin = skin;
    }
}
