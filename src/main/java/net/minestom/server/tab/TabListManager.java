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
     * This method sends the player info to the viewer if the displayedPlayer isn't on their TabList
     *
     * This resolves issues with skins not loading if the displayedPlayer isn't on the viewers TabList
     *
     * @param displayedPlayer the player being viewed
     * @param viewer the player that is viewing the displayedPlayer
     */
    public void handleSkinInView(Player displayedPlayer, Player viewer) {
        if (!viewer.getTabList().getDisplayedPlayers().contains(displayedPlayer)) {
            PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);
            PlayerInfoPacket.AddPlayer addPlayer = new PlayerInfoPacket.AddPlayer(displayedPlayer.getUuid(), displayedPlayer.getUsername(), displayedPlayer.getGameMode(), displayedPlayer.getLatency());
            addPlayer.displayName = displayedPlayer.getDisplayName();

            // Skin support
            if (displayedPlayer.getSkin() != null) {
                final String textures = displayedPlayer.getSkin().getTextures();
                final String signature = displayedPlayer.getSkin().getSignature();
                new PlayerInfoPacket.AddPlayer.Property("textures", textures, signature);
                addPlayer.properties.add(new PlayerInfoPacket.AddPlayer.Property("textures", textures, signature));
            }
            playerInfoPacket.playerInfos.add(addPlayer);
            viewer.getPlayerConnection().sendPacket(playerInfoPacket);

            PlayerInfoPacket playerRemoveInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);
            PlayerInfoPacket.RemovePlayer removePlayer = new PlayerInfoPacket.RemovePlayer(displayedPlayer.getUuid());
            playerRemoveInfoPacket.playerInfos.add(removePlayer);
            viewer.getPlayerConnection().sendPacket(playerRemoveInfoPacket);
        }
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
            if (tabList.doesLatencyUpdate() && tabList.getDisplayedPlayers().contains(player)) {
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
            if (tabList.doesGamemodeUpdates() && tabList.getDisplayedPlayers().contains(player)) {
                PacketUtils.sendGroupedPacket(tabList.getViewers(), playerInfoPacket);
            }
        }
    }
}
