package net.minestom.server.tab;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class TabList {

    private final List<Player> players = new CopyOnWriteArrayList<>();
    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private JsonMessage header;
    private JsonMessage footer;

    private boolean latencyUpdates = true;
    private boolean gamemodeUpdates = true;

    TabList() {
    }

    /**
     * Gets all the players that are displayed on the TabList
     *
     * @return A copied List of players that are displayed on the TabList, or empty List if none
     */
    @NotNull
    public List<Player> getPlayers() {
        return new CopyOnWriteArrayList<>(this.players);
    }

    /**
     * Gets all the players that are viewing this TabList
     *
     * @return A copied Set of players that are viewing the TabList, or empty Set if none
     */
    @NotNull
    public Set<Player> getViewers() {
        return new CopyOnWriteArraySet<>(this.viewers);
    }

    /**
     * The TabList header
     *
     * @return the content of the header
     */
    @Nullable
    public JsonMessage getHeader() {
        return this.header;
    }

    /**
     * Sets the header content of the TabList
     *
     * @param header the new header content
     */
    public void setHeader(@Nullable JsonMessage header) {
        this.header = header;
        PacketUtils.sendGroupedPacket(this.viewers, this.generateHeaderAndFooterPacket());
    }

    /**
     * The TabList footer
     * This will not update the tab list immediately so you must call {TabList#update}
     *
     * @return the content of the footer
     */
    @Nullable
    public JsonMessage getFooter() {
        return this.footer;
    }

    /**
     * Sets the footer content of the TabList.
     * This will not update the tab list immediately so you must call {TabList#update}
     *
     * @param footer the new footer content
     */
    public void setFooter(@Nullable JsonMessage footer) {
        this.footer = footer;
        PacketUtils.sendGroupedPacket(this.viewers, this.generateHeaderAndFooterPacket());
    }

    private PlayerListHeaderAndFooterPacket generateHeaderAndFooterPacket() {
        PlayerListHeaderAndFooterPacket playerListHeaderAndFooterPacket = new PlayerListHeaderAndFooterPacket();
        playerListHeaderAndFooterPacket.footer = this.footer;
        playerListHeaderAndFooterPacket.header = this.header;
        return playerListHeaderAndFooterPacket;
    }

    /**
     * Adds a player to be displayed on this TabList
     *
     * @param player the player to be added
     */
    public void addPlayer(Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);

        PlayerInfoPacket.AddPlayer addPlayer =
                new PlayerInfoPacket.AddPlayer(player.getUuid(), player.getUsername(), player.getGameMode(), player.getLatency());
        addPlayer.displayName = player.getDisplayName();

        // Skin support
        if (player.getSkin() != null) {
            final String textures = player.getSkin().getTextures();
            final String signature = player.getSkin().getSignature();

            PlayerInfoPacket.AddPlayer.Property prop =
                    new PlayerInfoPacket.AddPlayer.Property("textures", textures, signature);
            addPlayer.properties.add(prop);
        }

        playerInfoPacket.playerInfos.add(addPlayer);

        PacketUtils.sendGroupedPacket(this.viewers, playerInfoPacket);

        this.players.add(player);
    }

    /**
     * Removes a player from being displayed on this TabList
     *
     * @param player the player to be removed
     */
    public void removePlayer(Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);

        PlayerInfoPacket.RemovePlayer removePlayer =
                new PlayerInfoPacket.RemovePlayer(player.getUuid());

        playerInfoPacket.playerInfos.add(removePlayer);

        PacketUtils.sendGroupedPacket(this.viewers, playerInfoPacket);
        this.players.remove(player);
    }

    /**
     * Changes a player's tab list instance to this class and removes their old TabList instance.
     *
     * @param player the player to change the TabList for
     */
    public void addViewer(@NotNull Player player) {
        if (player.getTabList() != null) {
            player.getTabList().removeViewer(player);
        }
        this.viewers.add(player);
        player.setTabList(this);
        player.sendPacketToViewersAndSelf(this.generateHeaderAndFooterPacket());
    }

    protected void removeViewer(@NotNull Player player) {
        this.viewers.remove(player);
    }

    /**
     * Sends a packet to all viewers of the TabList telling them to display
     * the gamemode of the specified player as the specified gamemode
     * <p>
     * This is used to communicate part of a player's actual gamemode so it's recommended you only use this method with a {@link net.minestom.server.entity.fakeplayer.FakePlayer}
     *
     * @param player   The player to set the fake gamemode for
     * @param gameMode The gamemode to send for the specified player
     * @throws IllegalStateException if the player is not on the TabList or the TabList is set to automatically update gamemodes
     */
    public void setFakeGamemode(Player player, GameMode gameMode) {
        if (this.gamemodeUpdates)
            throw new IllegalStateException("Cannot set fake gamemode unless gamemodeUpdates is set to false");
        if (!this.players.contains(player))
            throw new IllegalStateException("Cannot set fake gamemode for a player not displayed on the TabList");
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateGamemode(player.getUuid(), gameMode));

        PacketUtils.sendGroupedPacket(this.viewers, playerInfoPacket);
    }

    /**
     * Sends a packet to all viewers of the TabList telling them to display
     * the latency of the specified player as the specified latency
     *
     * @param player  The player to set the fake latency for
     * @param latency The latency to send for the specified player
     * @throws IllegalStateException if the player is not on the TabList or the TabList is set to automatically update latency
     */
    public void setFakeLatency(Player player, int latency) {
        if (this.latencyUpdates)
            throw new IllegalStateException("Cannot set fake latency unless latencyUpdates is set to false");
        if (!this.players.contains(player))
            throw new IllegalStateException("Cannot set fake latency for a player not displayed on the TabList");

        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(player.getUuid(), latency));

        PacketUtils.sendGroupedPacket(this.viewers, playerInfoPacket);
    }

    /**
     * Whether the TabList is configured to automatically send player latency
     * updates to all its viewers
     *
     * @return boolean whether player latency will be auto-updated on TabLists
     */
    public boolean isLatencyUpdates() {
        return this.latencyUpdates;
    }

    /**
     * Sets whether the TabList will send packets when a user's latency is updated.
     * This would occur when a keepalive packet is received.
     *
     * @param latencyUpdates true/false whether or not latency updates will be sent
     */
    public void setLatencyUpdates(boolean latencyUpdates) {
        this.latencyUpdates = latencyUpdates;
    }

    /**
     * Whether the TabList is configured to automatically send player gamemode
     * updates to all its viewers
     *
     * @return boolean whether player gamemode will be auto-updated on TabLists
     */
    public boolean isGamemodeUpdates() {
        return this.gamemodeUpdates;
    }

    /**
     * Sets whether the TabList will send packets when a user's gamemode is updated.
     *
     * @param gamemodeUpdates true/false whether or not gamemode updates will be sent
     */
    public void setGamemodeUpdates(boolean gamemodeUpdates) {
        this.gamemodeUpdates = gamemodeUpdates;
    }
}
