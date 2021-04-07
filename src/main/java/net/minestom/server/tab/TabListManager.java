package net.minestom.server.tab;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TabListManager {
    private final Set<TabList> tabLists = new CopyOnWriteArraySet<>();
    private TabList defaultTabList = this.createTabList();

    /**
     * Creates a new TabList
     *
     * @return a newly created TabList
     */
    public @NotNull TabList createTabList() {
        TabList tablist = new TabList();
        this.tabLists.add(tablist);
        return tablist;
    }

    /**
     * Gets the registered TabLists
     *
     * @return the registered {@link TabList}s
     */
    public @NotNull Set<TabList> getTabLists() {
        return this.tabLists;
    }

    /**
     * This TabList is assigned to a player when they log in.
     *
     * @return the default TabList
     */
    public @NotNull TabList getDefaultTabList() {
        return this.defaultTabList;
    }

    /**
     * Sets the TabList that is assigned to a player when they log in
     *
     * @param defaultTabList the TabList to set the default TabList to
     */
    public void setDefaultTabList(@NotNull TabList defaultTabList) {
        this.defaultTabList = defaultTabList;
    }

    /**
     * Updates the latency of the player in all TabLists that the player is displayed
     *
     * @param player the updated player
     */
    public void updateLatency(@NotNull Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(player.getUuid(), player.getLatency()));


        for (TabList tabList : this.tabLists) {
            if (tabList.isLatencyUpdates() && tabList.getDisplayedPlayers().contains(player)) {
                PacketUtils.sendGroupedPacket(tabList.getViewers(), playerInfoPacket);
            }
        }
    }

    /**
     * Updates a player's gamemode for all viewers of the TabList
     *
     * @param player The player to update the gamemode info for
     */
    public void updateGamemode(@NotNull Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateGamemode(player.getUuid(), player.getGameMode()));

        for (TabList tabList : this.tabLists) {
            if (tabList.isGamemodeUpdates() && tabList.getDisplayedPlayers().contains(player)) {
                PacketUtils.sendGroupedPacket(tabList.getViewers(), playerInfoPacket);
            }
        }
    }
}
