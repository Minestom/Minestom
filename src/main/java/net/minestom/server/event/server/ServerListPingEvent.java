package net.minestom.server.event.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ResponseData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public class ServerListPingEvent extends Event implements CancellableEvent {
    private boolean cancelled = false;

    private final ResponseData responseData;
    private final PlayerConnection connection;


    public ServerListPingEvent(ResponseData responseData, PlayerConnection connection) {
        this.responseData = responseData;
        this.connection = connection;
    }

    /**
     * ResponseData being returned.
     *
     * @return the response data being returned
     */
    public @NotNull ResponseData getResponseData() {
        return responseData;
    }


    /**
     * PlayerConnection of received packet.
     *
     * Note that the player has not joined the server at this time.
     *
     * @return the playerConnection.
     */

    public @NotNull PlayerConnection getConnection() {
        return connection;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancelling this event will cause you server to appear offline in the vanilla server list.
     *
     * @param cancel true if the event should be cancelled, false otherwise
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    // Shortcut Methods
    /**
     * Sets the version name for the response.
     *
     * @param version The version name for the response data.
     */
    public void setVersion(@NotNull String version) {
        responseData.setVersion(version);
    }

    /**
     * Sets the response protocol version.
     *
     * @param protocol The protocol version for the response data.
     */
    public void setProtocol(int protocol) {
        responseData.setProtocol(protocol);
    }

    /**
     * Sets the response maximum player count.
     *
     * @param maxPlayer The maximum player count for the response data.
     */
    public void setMaxPlayer(int maxPlayer) {
        responseData.setMaxPlayer(maxPlayer);
    }

    /**
     * Sets the response online count.
     *
     * @param online The online count for the response data.
     */
    public void setOnline(int online) {
        responseData.setOnline(online);
    }

    /**
     * Adds some players to the response.
     *
     * @param players the players
     */
    public void addPlayer(@NotNull Iterable<Player> players) {
        responseData.addPlayer(players);
    }

    /**
     * Adds a player to the response.
     *
     * @param player the player
     */
    public void addPlayer(@NotNull Player player) {
        addPlayer(player.getUsername(), player.getUuid());
    }

    /**
     * Adds a player to the response.
     *
     * @param name The name of the player.
     * @param uuid The unique identifier of the player.
     */
    public void addPlayer(@NotNull String name, @NotNull UUID uuid) {
        responseData.addPlayer(name, uuid);
    }

    /**
     * Adds a player to the response.
     * {@link UUID#randomUUID()} is used as a default parameter for uuid
     *
     * @param name The name of the player.
     */
    public void addPlayer(String name) {
        responseData.addPlayer(name, UUID.randomUUID());
    }

    /**
     * Removes all of the ping players from this {@link #responseData#pingPlayers}. The {@link #responseData#pingPlayers} list
     * will be empty this call returns.
     */
    public void clearPlayers() {
        responseData.clearPlayers();
    }

    /**
     * Sets the response description.
     *
     * @param description The description for the response data.
     */
    public void setDescription(Component description) {
        responseData.setDescription(description);
    }

    /**
     * Sets the response favicon.
     *
     * MUST start with "data:image/png;base64,"
     *
     * @param favicon The favicon for the response data.
     */
    public void setFavicon(String favicon) {
        responseData.setFavicon(favicon);
    }

    /**
     * Get the server address a client used to connect.
     *
     * @return the server address
     */
    public @Nullable String getRemoteServerAddress() {
        return connection.getServerAddress();
    }

    /**
     * Get the server port a client used to connect.
     *
     * @return the server port
     */
    public int getServerPort() {
        return connection.getServerPort();
    }

    /**
     * Get the protocol version a client used to connect.
     *
     * @return the protocol version
     */
    public int getClientProtocolVersion() {
        if (connection instanceof NettyPlayerConnection) {
            return connection.getProtocolVersion();
        }
        return MinecraftServer.PROTOCOL_VERSION;
    }

}
