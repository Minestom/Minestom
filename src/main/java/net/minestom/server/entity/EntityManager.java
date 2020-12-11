package net.minestom.server.entity;

import com.google.common.collect.Queues;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPreLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

public final class EntityManager {

    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    private static final long KEEP_ALIVE_DELAY = 10_000;
    private static final long KEEP_ALIVE_KICK = 30_000;
    private static final ColoredText TIMEOUT_TEXT = ColoredText.of(ChatColor.RED + "Timeout");

    private final Queue<Player> waitingPlayers = Queues.newConcurrentLinkedQueue();

    /**
     * Connects waiting players.
     */
    public void updateWaitingPlayers() {
        // Connect waiting players
        waitingPlayersTick();
    }

    /**
     * Updates keep alive by checking the last keep alive packet and send a new one if needed.
     *
     * @param tickStart the time of the update in milliseconds, forwarded to the packet
     */
    public void handleKeepAlive(long tickStart) {
        final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(tickStart);
        for (Player player : CONNECTION_MANAGER.getOnlinePlayers()) {
            final long lastKeepAlive = tickStart - player.getLastKeepAlive();
            if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                player.refreshKeepAlive(tickStart);
                player.getPlayerConnection().sendPacket(keepAlivePacket);
            } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                player.kick(TIMEOUT_TEXT);
            }
        }
    }

    /**
     * Adds connected clients after the handshake (used to free the networking threads).
     */
    private void waitingPlayersTick() {
        Player waitingPlayer;
        while ((waitingPlayer = waitingPlayers.poll()) != null) {

            PlayerLoginEvent loginEvent = new PlayerLoginEvent(waitingPlayer);
            waitingPlayer.callEvent(PlayerLoginEvent.class, loginEvent);
            final Instance spawningInstance = loginEvent.getSpawningInstance();

            Check.notNull(spawningInstance, "You need to specify a spawning instance in the PlayerLoginEvent");

            waitingPlayer.init();

            // Spawn the player at Player#getRespawnPoint during the next instance tick
            spawningInstance.scheduleNextTick(waitingPlayer::setInstance);
        }
    }

    /**
     * Calls the player initialization callbacks and the event {@link PlayerPreLoginEvent}.
     * If the {@link Player} hasn't been kicked, add him to the waiting list.
     * <p>
     * Can be considered as a pre-init thing,
     * currently executed in {@link Player#Player(UUID, String, PlayerConnection)}.
     *
     * @param player the {@link Player} to add
     */
    public void addWaitingPlayer(@NotNull Player player) {

        // Init player (register events)
        for (Consumer<Player> playerInitialization : MinecraftServer.getConnectionManager().getPlayerInitializations()) {
            playerInitialization.accept(player);
        }

        // Call pre login event
        PlayerPreLoginEvent playerPreLoginEvent = new PlayerPreLoginEvent(player, player.getUsername(), player.getUuid());
        player.callEvent(PlayerPreLoginEvent.class, playerPreLoginEvent);

        // Ignore the player if he has been disconnected (kick)
        final boolean online = player.isOnline();
        if (!online)
            return;

        // Add him to the list and change his username/uuid if changed
        this.waitingPlayers.add(player);

        final String username = playerPreLoginEvent.getUsername();
        final UUID uuid = playerPreLoginEvent.getPlayerUuid();

        player.setUsername(username);
        player.setUuid(uuid);
    }
}
