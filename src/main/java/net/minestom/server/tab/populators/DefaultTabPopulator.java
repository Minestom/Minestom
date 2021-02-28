package net.minestom.server.tab.populators;

import net.minestom.server.entity.Player;
import net.minestom.server.tab.TabList;
import net.minestom.server.tab.TabListManager;
import net.minestom.server.tab.TabListPopulator;

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

    //TODO document
    public TabList getDefaultTabList() {
        return this.defaultTabList;
    }
}
