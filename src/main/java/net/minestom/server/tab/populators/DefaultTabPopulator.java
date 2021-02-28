package net.minestom.server.tab.populators;

import net.minestom.server.entity.Player;
import net.minestom.server.tab.TabList;
import net.minestom.server.tab.TabListManager;
import net.minestom.server.tab.TabListPopulator;

/**
 * Creates a single {@link TabList} instance that will be used for all players.
 * All players are viewers & displayed on the TabList.
 */
public class DefaultTabPopulator implements TabListPopulator {
    private final TabList defaultTabList;

    public DefaultTabPopulator(TabListManager manager) {
        this.defaultTabList = manager.createTabList();
    }

    @Override
    public void onJoin(Player player) {
        this.defaultTabList.addViewer(player);
        this.defaultTabList.addPlayer(player);
        player.setTabList(this.defaultTabList);
    }

    /**
     * Gets the TabList assigned to every user on join when this populator is active
     *
     * @return assigned {@link TabList}
     */
    public TabList getDefaultTabList() {
        return this.defaultTabList;
    }
}
