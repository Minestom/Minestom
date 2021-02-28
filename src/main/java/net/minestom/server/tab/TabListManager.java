package net.minestom.server.tab;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.utils.PacketUtils;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TabListManager {

    private final Set<TabList> tablists = new CopyOnWriteArraySet<>();

    public void createTablist() {
        TabList tablist = new TabList();
        this.tablists.add(tablist);
    }

    public Set<TabList> getTablists() {
        return tablists;
    }


    /**
     * Updates the latency of the player in all TabLists that the player is displayed
     *
     * @param player the updated player
     */
    public void updateLatency(Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(player.getUuid(), player.getLatency()));


        for (TabList tabList : tablists) {
            if (tabList.getPlayers().contains(player)) {
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

        for (TabList tabList : tablists) {
            if (tabList.getPlayers().contains(player)) {
                PacketUtils.sendGroupedPacket(tabList.getViewers(), playerInfoPacket);
            }
        }

    }

    //TODO make the manager accessible in the player class so the above methods can be called when the variable changes


}
