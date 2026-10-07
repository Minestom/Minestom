package net.minestom.server.network;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.listener.preplay.LoginListener;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.common.DisconnectPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class ConnectionManagerIntegrationTest {

    private GameProfile[] profiles;

    @BeforeEach
    public void setup(Env env) {
        profiles = new GameProfile[]{
                new GameProfile(UUID.randomUUID(), "Minestom"),
                new GameProfile(UUID.randomUUID(), "Notch")};
    }

    @Test
    public void testPartialFind(Env env) {
        Instance instance = env.createEmptyInstance();
        Player minestomPlayer = env.createConnection(profiles[0]).connect(instance, Pos.ZERO);
        ConnectionManager connectionManager = env.process().connection();

        assertEquals(minestomPlayer, connectionManager.findOnlinePlayer("Mine"));
        assertNull(connectionManager.findOnlinePlayer("No"));

        Player notchPlayer = env.createConnection(profiles[1]).connect(instance, Pos.ZERO);

        assertEquals(minestomPlayer, connectionManager.findOnlinePlayer("Mine"));
        assertEquals(notchPlayer, connectionManager.findOnlinePlayer("No"));
        assertNull(connectionManager.findOnlinePlayer("leo"));
    }

    @Test
    public void profileIsPublishedBeforeLoginSuccess(Env env) throws IOException {
        final GameProfile profile = profiles[0];

        try (SocketChannel channel = SocketChannel.open()) {
            final var connection = new ProfileCapturingConnection(channel);
            connection.setClientState(ConnectionState.LOGIN);

            final CompletableFuture<GameProfile> future = new CompletableFuture<>();
            Thread.startVirtualThread(() -> {
                try {
                    future.complete(env.process().connection().transitionLoginToConfig(connection, profiles[0]));
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
            final GameProfile result = future.join();

            assertSame(profile, result);
            assertSame(profile, connection.profileWhenLoginSuccessSent.join());
            assertSame(profile, connection.gameProfile());
        }
    }

    @Test
    public void earlyFinishConfigurationIsRejected(Env env) {
        final ConnectionManager connectionManager = env.process().connection();
        final RecordingConnection connection = new RecordingConnection();
        connection.setClientState(ConnectionState.CONFIGURATION);
        connection.setServerState(ConnectionState.CONFIGURATION);
        final Player player = connectionManager.createPlayer(connection, profiles[0]);

        // The client finishes before the server sent its own finish packet, so no spawning instance exists yet
        LoginListener.finishConfigListener(new ClientFinishConfigurationPacket(), player);

        assertFalse(connection.isOnline());
        assertInstanceOf(DisconnectPacket.class, connection.packets.getLast());
        assertDoesNotThrow(env::tick);
        assertTrue(connectionManager.getOnlinePlayers().isEmpty());
    }

    @Test
    public void spawnFailureKicksOnlyThatPlayer(Env env) {
        final ConnectionManager connectionManager = env.process().connection();
        final List<Throwable> failures = new ArrayList<>();
        env.process().exception().setExceptionHandler(failures::add);
        final Instance instance = env.createEmptyInstance();

        connectionManager.setPlayerProvider(FailingSpawnPlayer::new);
        final RecordingConnection failingConnection = new RecordingConnection();
        enterPlay(connectionManager, failingConnection, profiles[0], instance);
        connectionManager.setPlayerProvider(Player::new);
        final RecordingConnection otherConnection = new RecordingConnection();
        final Player other = enterPlay(connectionManager, otherConnection, profiles[1], instance);

        assertDoesNotThrow(env::tick);
        assertEquals(1, failures.size());
        assertInstanceOf(IllegalStateException.class, failures.getFirst());
        assertFalse(failingConnection.isOnline());
        assertTrue(otherConnection.isOnline());
        assertEquals(Set.of(other), connectionManager.getOnlinePlayers());
        assertEquals(instance, other.getInstance());
    }

    /**
     * Runs the configuration phase for a new player and acknowledges its end as the client would.
     */
    private static Player enterPlay(ConnectionManager connectionManager, RecordingConnection connection,
                                    GameProfile profile, Instance instance) {
        connection.setClientState(ConnectionState.CONFIGURATION);
        connection.setServerState(ConnectionState.CONFIGURATION);
        final Player player = connectionManager.createPlayer(connection, profile);
        player.eventNode().addListener(AsyncPlayerConfigurationEvent.class, event -> event.setSpawningInstance(instance));
        // The configuration event must be called from a virtual thread
        CompletableFuture.runAsync(() -> connectionManager.doConfiguration(player, false), Thread.ofVirtual()::start).join();
        // The recording connection does not follow the state changes of the packets it sends
        connection.setClientState(ConnectionState.PLAY);
        connection.setServerState(ConnectionState.PLAY);
        LoginListener.finishConfigListener(new ClientFinishConfigurationPacket(), player);
        return player;
    }

    private static final class RecordingConnection extends PlayerConnection {
        private final List<SendablePacket> packets = new ArrayList<>();

        @Override
        public void sendPacket(SendablePacket packet) {
            packets.add(packet);
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return new InetSocketAddress("localhost", 25565);
        }
    }

    private static final class FailingSpawnPlayer extends Player {
        private FailingSpawnPlayer(PlayerConnection connection, GameProfile gameProfile) {
            super(connection, gameProfile);
        }

        @Override
        public CompletableFuture<Void> UNSAFE_init() {
            throw new IllegalStateException("Spawn failure");
        }
    }

    private static final class ProfileCapturingConnection extends PlayerSocketConnection {
        private final CompletableFuture<GameProfile> profileWhenLoginSuccessSent = new CompletableFuture<>();

        private ProfileCapturingConnection(SocketChannel channel) {
            super(channel, new InetSocketAddress("localhost", 25565),
                    Thread.currentThread(), Thread.currentThread());
        }

        @Override
        public void sendPacket(SendablePacket packet) {
            if (packet instanceof LoginSuccessPacket) {
                // Model the socket reader handling an immediate acknowledgement before the login
                // thread resumes from sendPacket.
                Thread.startVirtualThread(() -> profileWhenLoginSuccessSent.complete(gameProfile()));
                profileWhenLoginSuccessSent.join();
            }
        }
    }

}
