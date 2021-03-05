package net.minestom.server.tab.populators;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.tab.TabList;
import net.minestom.server.tab.TabListManager;
import net.minestom.server.tab.TabListPopulator;

/**
 * Creates a single {@link TabList} instance that will be used for all players.
 * All players are viewers and displayed on the TabList.
 */
public class DefaultTabPopulator implements TabListPopulator {
    private final TabList defaultTabList;

    public DefaultTabPopulator(TabListManager manager) {
        this.defaultTabList = manager.createTabList();
    }

    @Override
    public void init() {
        MinecraftServer.getGlobalEventHandler().addEventCallback(PlayerLoginEvent.class, event -> this.onJoin(event.getPlayer()));
        MinecraftServer.getGlobalEventHandler().addEventCallback(PlayerDisconnectEvent.class, event -> this.onLeave(event.getPlayer()));
    }

    @Override
    public void onJoin(Player player) {
        this.defaultTabList.addViewer(player);
        this.defaultTabList.addDisplayedPlayer(player);
        player.setTabList(this.defaultTabList);
    }

    @Override
    public void onLeave(Player player) {
        this.defaultTabList.removeDisplayedPlayer(player);
        this.defaultTabList.removeViewer(player);
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
