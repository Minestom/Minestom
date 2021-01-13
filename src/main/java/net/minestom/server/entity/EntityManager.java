package net.minestom.server.entity;

import com.google.common.collect.Queues;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;

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
                final PlayerConnection playerConnection = player.getPlayerConnection();
                player.refreshKeepAlive(tickStart);
                playerConnection.sendPacket(keepAlivePacket);
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

            waitingPlayer.init(spawningInstance);

            // Spawn the player at Player#getRespawnPoint during the next instance tick
            spawningInstance.scheduleNextTick(waitingPlayer::setInstance);
        }
    }

    /**
     * Adds a player into the waiting list, to be handled during the next server tick.
     *
     * @param player the {@link Player player} to add into the waiting list
     */
    public void addWaitingPlayer(@NotNull Player player) {
        this.waitingPlayers.add(player);
    }
}
