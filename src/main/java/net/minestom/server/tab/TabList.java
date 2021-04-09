package net.minestom.server.tab;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class TabList implements Viewable {

    private final List<Player> displayedPlayers = new CopyOnWriteArrayList<>();
    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private Component header;
    private Component footer;

    private boolean latencyUpdates = true;
    private boolean gamemodeUpdates = true;

    /**
     * You must use {@link TabListManager#createTabList()}
     */
    TabList() {
    }

    /**
     * Gets all the players that are displayed on the TabList
     *
     * @return A copied unmodifiable List of players that are displayed on the TabList, or an empty List if none
     */
    public @NotNull List<Player> getDisplayedPlayers() {
        return Collections.unmodifiableList(this.displayedPlayers);
    }

    /**
     * Gets all the players that are viewing this TabList
     *
     * @return A copied unmodifiable Set of players that are viewing the TabList, or an empty Set if none
     */
    public @NotNull Set<Player> getViewers() {
        return Collections.unmodifiableSet(this.viewers);
    }

    /**
     * The TabList header
     *
     * @return the content of the header
     */
    public @Nullable Component getHeader() {
        return this.header;
    }

    /**
     * Sets the header content of the TabList
     *
     * @param header the new header content
     */
    public void setHeader(@Nullable Component header) {
        this.header = header;
        this.sendPacketToViewers(this.generateHeaderAndFooterPacket());
    }

    /**
     * The TabList footer
     * This will not update the tab list immediately so you must call {TabList#update}
     *
     * @return the content of the footer
     */
    public @Nullable Component getFooter() {
        return this.footer;
    }

    /**
     * Sets the footer content of the TabList.
     * This will not update the tab list immediately so you must call {TabList#update}
     *
     * @param footer the new footer content
     */
    public void setFooter(@Nullable Component footer) {
        this.footer = footer;
        this.sendPacketToViewers(this.generateHeaderAndFooterPacket());
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
    public void addDisplayedPlayer(@NotNull Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);

        PlayerInfoPacket.AddPlayer addPlayer =
                new PlayerInfoPacket.AddPlayer(player.getUuid(), player.getUsername(), player.getGameMode(), player.getLatency());
        addPlayer.displayName = player.getDisplayName();

        // Skin support
        if (player.getSkin() != null) {
            addPlayer.properties.add(this.createPropertyPacket(player));
        }

        playerInfoPacket.playerInfos.add(addPlayer);
        this.sendPacketToViewers(playerInfoPacket);
        this.displayedPlayers.add(player);
    }

    /**
     * Adds players to be displayed on this TabList
     *
     * @param players the players to be added
     */
    public void addDisplayedPlayers(@NotNull Player... players) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);

        for (Player player : players) {
            PlayerInfoPacket.AddPlayer addPlayer =
                    new PlayerInfoPacket.AddPlayer(player.getUuid(), player.getUsername(), player.getGameMode(), player.getLatency());
            addPlayer.displayName = player.getDisplayName();

            // Skin support
            if (player.getSkin() != null) {
                addPlayer.properties.add(this.createPropertyPacket(player));
            }

            playerInfoPacket.playerInfos.add(addPlayer);
            this.displayedPlayers.add(player);
        }

        this.sendPacketToViewers(playerInfoPacket);
    }

    /**
     * Removes a player from being displayed on this TabList
     *
     * @param player the player to be removed
     */
    public void removeDisplayedPlayer(@NotNull Player player) {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);

        PlayerInfoPacket.RemovePlayer removePlayer = new PlayerInfoPacket.RemovePlayer(player.getUuid());

        playerInfoPacket.playerInfos.add(removePlayer);

        this.sendPacketToViewers(playerInfoPacket);
        this.displayedPlayers.remove(player);
    }

    /**
     * Changes a player's tab list instance to this class and removes their old TabList instance.
     *
     * @param player the player to change the TabList for
     * @return true
     */
    public boolean addViewer(@NotNull Player player) {
        player.getTabList().removeViewer(player);
        this.viewers.add(player);
        player.setTabList(this);

        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);

        for (Player displayedPlayer : this.displayedPlayers) {
            PlayerInfoPacket.AddPlayer addPlayer =
                    new PlayerInfoPacket.AddPlayer(displayedPlayer.getUuid(), displayedPlayer.getUsername(), displayedPlayer.getGameMode(), displayedPlayer.getLatency());
            addPlayer.displayName = displayedPlayer.getDisplayName();

            // Skin support
            if (displayedPlayer.getSkin() != null) {
                addPlayer.properties.add(this.createPropertyPacket(player));
            }

            playerInfoPacket.playerInfos.add(addPlayer);
        }
        player.getPlayerConnection().sendPacket(playerInfoPacket);
        this.sendPacketToViewers(this.generateHeaderAndFooterPacket());
        return true;
    }

    public boolean removeViewer(@NotNull Player player) {
        return this.viewers.remove(player);
    }

    /**
     * Updates the display name of a player to their current display name.
     * This is automatically called when {@link Player#setDisplayName(Component)} is called.
     *
     * @param player The player to update the display name for
     */
    public void updateDisplayName(@NotNull Player player) {
        PlayerInfoPacket infoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME);
        infoPacket.playerInfos.add(new PlayerInfoPacket.UpdateDisplayName(player.getUuid(), player.getDisplayName()));
        this.sendPacketToViewers(infoPacket);
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
    public void setDisplayedGamemode(@NotNull Player player, @NotNull GameMode gameMode) {
        if (this.gamemodeUpdates)
            throw new IllegalStateException("Cannot set fake gamemode unless gamemodeUpdates is set to false");
        if (!this.displayedPlayers.contains(player))
            throw new IllegalStateException("Cannot set fake gamemode for a player not displayed on the TabList");
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateGamemode(player.getUuid(), gameMode));

        this.sendPacketToViewers(playerInfoPacket);
    }

    /**
     * Sends a packet to all viewers of the TabList telling them to display
     * the latency of the specified player as the specified latency
     *
     * @param player  The player to set the fake latency for
     * @param latency The latency to send for the specified player
     * @throws IllegalStateException if the player is not on the TabList or the TabList is set to automatically update latency
     */
    public void setDisplayedPing(@NotNull Player player, int latency) {
        if (this.latencyUpdates)
            throw new IllegalStateException("Cannot set fake latency unless latencyUpdates is set to false");
        if (!this.displayedPlayers.contains(player))
            throw new IllegalStateException("Cannot set fake latency for a player not displayed on the TabList");

        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(player.getUuid(), latency));


        this.sendPacketToViewers(playerInfoPacket);
    }

    private PlayerInfoPacket.AddPlayer.Property createPropertyPacket(Player player) {
        final String textures = player.getSkin().getTextures();
        final String signature = player.getSkin().getSignature();

        return new PlayerInfoPacket.AddPlayer.Property("textures", textures, signature);
    }

    /**
     * Whether the TabList is configured to automatically send player latency
     * updates to all its viewers
     *
     * @return boolean whether player latency will be auto-updated on TabLists
     */
    public boolean doesLatencyUpdate() {
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
    public boolean doesGamemodeUpdates() {
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
