package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPreLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.validate.Check;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class EntityManager {

    private final ConcurrentLinkedQueue<Player> waitingPlayers = new ConcurrentLinkedQueue<>();

    /**
     * Connect waiting players
     */
    public void updateWaitingPlayers() {
        // Connect waiting players
        waitingPlayersTick();
    }

    /**
     * Add connected clients after the handshake (used to free the networking threads)
     */
    private void waitingPlayersTick() {
        Player waitingPlayer;
        while ((waitingPlayer = waitingPlayers.poll()) != null) {
            waitingPlayer.init();

            PlayerLoginEvent loginEvent = new PlayerLoginEvent(waitingPlayer);
            waitingPlayer.callEvent(PlayerLoginEvent.class, loginEvent);
            final Instance spawningInstance = loginEvent.getSpawningInstance();

            Check.notNull(spawningInstance, "You need to specify a spawning instance in the PlayerLoginEvent");

            waitingPlayer.setInstance(spawningInstance);
        }
    }

    /**
     * Call the player initialization callbacks and the event {@link PlayerPreLoginEvent}
     * If the player hasn't been kicked, add him to the waiting list
     * <p>
     * Can be considered as a pre-init thing
     *
     * @param player the player to add
     */
    public void addWaitingPlayer(Player player) {

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
