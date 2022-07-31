package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

final class TestConnectionImpl implements TestConnection {
    private final Env env;
    private final ServerProcess process;
    private final PlayerConnectionImpl playerConnection = new PlayerConnectionImpl();

    private final List<IncomingCollector<SendablePacket>> incomingTrackers = new CopyOnWriteArrayList<>();

    TestConnectionImpl(Env env) {
        this.env = env;
        this.process = env.process();
    }

    @Override
    public @NotNull CompletableFuture<Player> connect(@NotNull Instance instance, @NotNull Pos pos) {
        Player player = new Player(UUID.randomUUID(), "RandName", playerConnection);
        player.eventNode().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(pos);
        });

        return process.connection().startPlayState(player, true)
                .thenApply(unused -> {
                    process.connection().updateWaitingPlayers();
                    return player;
                });
    }

    @Override
    public @NotNull <T extends SendablePacket> Collector<T> trackIncoming(@NotNull Class<T> type, boolean extractPackets) {
        var tracker = new IncomingCollector<>(type, extractPackets);
        this.incomingTrackers.add(IncomingCollector.class.cast(tracker));
        return tracker;
    }

    @Override
    public @NotNull <T extends SendablePacket> Collector<T> trackIncoming(@NotNull Class<T> type) {
        return this.trackIncoming(type, true);
    }

    final class PlayerConnectionImpl extends PlayerConnection {
        @Override
        public void sendPacket(@NotNull SendablePacket packet) {
            for (var tracker : incomingTrackers) {
                final var serverPacket = tracker.extractPacket ? SendablePacket.extractServerPacket(packet) : packet;
                if (tracker.type.isAssignableFrom(serverPacket.getClass())) tracker.packets.add(serverPacket);
            }
        }

        @Override
        public @NotNull SocketAddress getRemoteAddress() {
            return new InetSocketAddress("localhost", 25565);
        }

        @Override
        public void disconnect() {

        }
    }

    final class IncomingCollector<T extends SendablePacket> implements Collector<T> {
        private final Class<T> type;
        private final boolean extractPacket;
        private final List<T> packets = new CopyOnWriteArrayList<>();

        public IncomingCollector(Class<T> type, boolean extractPackets) {
            this.type = type;
            this.extractPacket = extractPackets;
        }

        @Override
        public @NotNull List<T> collect() {
            incomingTrackers.remove(this);
            return List.copyOf(packets);
        }
    }
}
