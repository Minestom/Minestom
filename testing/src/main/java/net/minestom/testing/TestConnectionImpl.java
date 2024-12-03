package net.minestom.testing;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

final class TestConnectionImpl implements TestConnection {
    private final Env env;
    private final ServerProcess process;
    private final PlayerConnectionImpl playerConnection = new PlayerConnectionImpl();

    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final List<IncomingCollector<ServerPacket>> incomingTrackers = new CopyOnWriteArrayList<>();

    TestConnectionImpl(Env env) {
        this.env = env;
        this.process = env.process();
    }

    @Override
    public @NotNull Player connect(@NotNull Instance instance, @NotNull Pos pos) {
        if (!connected.compareAndSet(false, true)) {
            throw new IllegalStateException("Already connected");
        }

        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "RandName");
        var player = process.connection().createPlayer(playerConnection, gameProfile);
        player.eventNode().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(pos);
        });

        // Force the player through the entirety of the login process manually
        CompletableFuture<Player> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            // `isFirstConfig` is set to false in order to not block the thread
            // waiting for known packs.
            // The consequence is that registry packets cannot be listened to.
            process.connection().doConfiguration(player, false);
            process.connection().transitionConfigToPlay(player);
            future.complete(player);
        });
        future.join();
        playerConnection.setConnectionState(ConnectionState.PLAY);
        process.connection().updateWaitingPlayers();
        return player;
    }

    @Override
    public @NotNull <T extends ServerPacket> Collector<T> trackIncoming(@NotNull Class<T> type) {
        var tracker = new IncomingCollector<>(type);
        this.incomingTrackers.add(IncomingCollector.class.cast(tracker));
        return tracker;
    }

    final class PlayerConnectionImpl extends PlayerConnection {
        private boolean online = true;

        @Override
        public void sendPacket(@NotNull SendablePacket packet) {
            final var serverPacket = this.extractPacket(packet);
            for (var tracker : incomingTrackers) {
                if (tracker.type.isAssignableFrom(serverPacket.getClass())) tracker.packets.add(serverPacket);
            }
        }

        private ServerPacket extractPacket(final SendablePacket packet) {
            if (!(packet instanceof ServerPacket serverPacket))
                return SendablePacket.extractServerPacket(getConnectionState(), packet);

            final Player player = getPlayer();
            if (player == null) return serverPacket;

            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && serverPacket instanceof ServerPacket.ComponentHolding) {
                serverPacket = ((ServerPacket.ComponentHolding) serverPacket).copyWithOperator(component ->
                        GlobalTranslator.render(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }

            return serverPacket;
        }

        @Override
        public @NotNull SocketAddress getRemoteAddress() {
            return new InetSocketAddress("localhost", 25565);
        }

        @Override
        public boolean isOnline() {
            return online;
        }

        @Override
        public void disconnect() {
            online = false;
        }
    }

    final class IncomingCollector<T extends ServerPacket> implements Collector<T> {
        private final Class<T> type;
        private final List<T> packets = new CopyOnWriteArrayList<>();

        public IncomingCollector(Class<T> type) {
            this.type = type;
        }

        @Override
        public @NotNull List<T> collect() {
            incomingTrackers.remove(this);
            return List.copyOf(packets);
        }
    }

    static final class TestPlayerImpl extends Player {
        public TestPlayerImpl(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
            super(playerConnection, gameProfile);
        }

        @Override
        public void sendChunk(@NotNull Chunk chunk) {
            // Send immediately
            sendPacket(chunk.getFullDataPacket());
        }
    }
}
