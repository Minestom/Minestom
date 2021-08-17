package net.minestom.server.extras.query.response;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.query.Query;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A basic query response containing a fixed set of responses.
 */
public class BasicQueryResponse implements Writeable {
    private String motd, gametype, map, numPlayers, maxPlayers;

    /**
     * Creates a new basic query response with pre-filled default values.
     */
    public BasicQueryResponse() {
        this.motd = "A Minestom Server";
        this.gametype = "SMP";
        this.map = "world";
        this.numPlayers = String.valueOf(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
        this.maxPlayers = String.valueOf(Integer.parseInt(this.numPlayers) + 1);
    }

    /**
     * Gets the MoTD.
     *
     * @return the motd
     */
    public @NotNull String getMotd() {
        return this.motd;
    }

    /**
     * Sets the MoTD.
     *
     * @param motd the motd
     */
    public void setMotd(@NotNull String motd) {
        this.motd = Objects.requireNonNull(motd, "motd");
    }

    /**
     * Gets the gametype.
     *
     * @return the gametype
     */
    public @NotNull String getGametype() {
        return this.gametype;
    }

    /**
     * Sets the gametype.
     *
     * @param gametype the gametype
     */
    public void setGametype(@NotNull String gametype) {
        this.gametype = Objects.requireNonNull(gametype, "gametype");
    }

    /**
     * Gets the map.
     *
     * @return the map
     */
    public @NotNull String getMap() {
        return this.map;
    }

    /**
     * Sets the map.
     *
     * @param map the map
     */
    public void setMap(@NotNull String map) {
        this.map = Objects.requireNonNull(map, "map");
    }

    /**
     * Gets the number of players.
     *
     * @return the number of players
     */
    public @NotNull String getNumPlayers() {
        return this.numPlayers;
    }

    /**
     * Sets the number of players.
     *
     * @param numPlayers the number of players
     */
    public void setNumPlayers(@NotNull String numPlayers) {
        this.numPlayers = Objects.requireNonNull(numPlayers, "numPlayers");
    }

    /**
     * Sets the number of players.
     * This method is just an overload for {@link #setNumPlayers(String)}.
     *
     * @param numPlayers the number of players
     */
    public void setNumPlayers(int numPlayers) {
        this.setNumPlayers(String.valueOf(numPlayers));
    }

    /**
     * Gets the max number of players.
     *
     * @return the max number of players
     */
    public @NotNull String getMaxPlayers() {
        return this.maxPlayers;
    }

    /**
     * Sets the max number of players.
     *
     * @param maxPlayers the max number of players
     */
    public void setMaxPlayers(@NotNull String maxPlayers) {
        this.maxPlayers = Objects.requireNonNull(maxPlayers, "maxPlayers");
    }

    /**
     * Sets the max number of players.
     * This method is just an overload for {@link #setMaxPlayers(String)}
     *
     * @param maxPlayers the max number of players
     */
    public void setMaxPlayers(int maxPlayers) {
        this.setMaxPlayers(String.valueOf(maxPlayers));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeNullTerminatedString(this.motd, Query.CHARSET);
        writer.writeNullTerminatedString(this.gametype, Query.CHARSET);
        writer.writeNullTerminatedString(this.map, Query.CHARSET);
        writer.writeNullTerminatedString(this.numPlayers, Query.CHARSET);
        writer.writeNullTerminatedString(this.maxPlayers, Query.CHARSET);
        writer.getBuffer().putShort((short) MinecraftServer.getServer().getPort()); // TODO little endian?
        writer.writeNullTerminatedString(Objects.requireNonNullElse(MinecraftServer.getServer().getAddress(), ""), Query.CHARSET);
    }
}
