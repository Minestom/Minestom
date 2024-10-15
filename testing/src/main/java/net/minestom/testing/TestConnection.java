package net.minestom.testing;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a connection to a test server.
 */
public interface TestConnection {

    /**
     * Sets the custom player provider for this test connection.
     * <br>
     * For a successful test you need to override the sendChunk method in the player implementation.
     * Example:
     * <pre>{@code
     * public class CustomGamePlayerImpl extends Player {
     *    public CustomGamePlayerImpl(UUID uuid, String username, PlayerConnection playerConnection) {
     *      super(uuid, username, playerConnection);
     *    }
     *    @Override
     *    public void sendChunk(Chunk chunk) {
     *      sendPacket(chunk.getFullDataPacket());
     *    }
     * }
     * }</pre>
     * @param provider the custom player provider
     */
    void setCustomPlayerProvider(@NotNull PlayerProvider provider);

    /**
     * Connects a player to the server.
     *
     * @param instance the instance to spawn the player in
     * @param pos      the position to spawn the player at
     * @return a future that completes when the player is connected
     */
    @NotNull CompletableFuture<@NotNull Player> connect(@NotNull Instance instance, @NotNull Pos pos);

    /**
     * Tracks incoming packets of a specific type.
     *
     * @param type the packet type to track
     * @param <T>  the packet type
     * @return a collector for the tracked packets
     */
    <T extends ServerPacket> @NotNull Collector<T> trackIncoming(@NotNull Class<T> type);

    /**
     * Tracks all incoming packets.
     *
     * @return a collector for all incoming packets
     */
    default @NotNull Collector<ServerPacket> trackIncoming() {
        return trackIncoming(ServerPacket.class);
    }
}
