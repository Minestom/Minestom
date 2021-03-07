package net.minestom.server.tab;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.tab.populators.DefaultTabPopulator;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TabListManager {

    private final Set<TabList> tabLists = new CopyOnWriteArraySet<>();
    private TabListPopulator tabListPopulator; // needs to be init to default at minimum

    public TabListManager() {
        this.setTabListPopulator(new DefaultTabPopulator(this));
        MinecraftServer.getGlobalEventHandler().addEventCallback(PlayerSpawnEvent.class, event -> this.tabListPopulator.onJoin(event.getPlayer()));
        MinecraftServer.getGlobalEventHandler().addEventCallback(PlayerDisconnectEvent.class, event -> this.tabListPopulator.onLeave(event.getPlayer()));
    }

    /**
     * Creates a new TabList
     *
     * @return a newly created TabList
     */
    public TabList createTabList() {
        TabList tablist = new TabList();
        this.tabLists.add(tablist);
        return tablist;
    }

    /**
     * Gets the registered TabLists
     *
     * @return the registered {@link TabList}s
     */
    public Set<TabList> getTabLists() {
        return this.tabLists;
    }

    /**
     * Gets the TabListPopulator used to determine what TabList users are displayed on join
     *
     * @return the currently determining {@link TabListPopulator}
     */
    public TabListPopulator getTabListPopulator() {
        return this.tabListPopulator;
    }

    /**
     * Sets the TabListPopulator used to determine what TabList users are displayed on join
     *
     * @param tabListPopulator the new {@link TabListPopulator} instance
     */
    public void setTabListPopulator(@NotNull TabListPopulator tabListPopulator) {
        this.tabListPopulator = tabListPopulator;
    }

    /**
     * Updates the latency of the player in all TabLists that the player is displayed
     *
     * @param player the updated player
     */
    public void updateLatency(Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(player.getUuid(), player.getLatency()));


        for (TabList tabList : this.tabLists) {
            if (tabList.isLatencyUpdates() && tabList.getDisplayedPlayers().contains(player)) {
                PacketUtils.sendGroupedPacket(tabList.getViewers(), playerInfoPacket);
            }
        }
    }

    /**
     * Updates a player's gamemode for all viewers of the tablist
     *
     * @param player The player to update the gamemode info for
     */
    public void updateGamemode(Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateGamemode(player.getUuid(), player.getGameMode()));

        for (TabList tabList : this.tabLists) {
            if (tabList.isGamemodeUpdates() && tabList.getDisplayedPlayers().contains(player)) {
                PacketUtils.sendGroupedPacket(tabList.getViewers(), playerInfoPacket);
            }
        }
    }

}
