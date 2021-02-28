package net.minestom.server.tab;

import net.minestom.server.entity.Player;

public interface TabListPopulator {

    /**
     * Called when a player joins the server and requests a TabList.
     * This should be used to set the TabList of a player on join.
     * Not setting the TabList for a player on join will likely cause issues.
     *
     * @param player the player the TabList will be set for
     */
    void onJoin(Player player);
}
