package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
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

    private final List<TrackerImpl<ServerPacket>> incomingTrackers = new CopyOnWriteArrayList<>();

    public TestConnectionImpl(Env env) {
        this.env = env;
        this.process = env.process();
    }

    @Override
    public CompletableFuture<Player> connect(Instance instance) {
        process.eventHandler().addListener(EventListener.builder(PlayerLoginEvent.class)
                .expireCount(1)
                .handler(event -> {
                    if (event.getPlayer().getPlayerConnection() == playerConnection) {
                        event.setSpawningInstance(instance);
                    }
                }).build());

        var player = new Player(UUID.randomUUID(), "RandName", playerConnection);
        process.connection().startPlayState(player, true);
        while (player.getInstance() != instance) { // TODO replace with proper future
            env.tick();
        }
        return CompletableFuture.completedFuture(player);
    }

    @Override
    public @NotNull <T extends ServerPacket> PacketTracker<T> trackIncoming(@NotNull Class<T> type) {
        var tracker = new TrackerImpl<>(type);
        this.incomingTrackers.add(TrackerImpl.class.cast(tracker));
        return tracker;
    }

    final class PlayerConnectionImpl extends PlayerConnection {
        @Override
        public void sendPacket(@NotNull SendablePacket packet) {
            for (var tracker : incomingTrackers) {
                final var serverPacket = SendablePacket.extractServerPacket(packet);
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

    final class TrackerImpl<T extends ServerPacket> implements PacketTracker<T> {
        private final Class<T> type;
        private final List<T> packets = new CopyOnWriteArrayList<>();

        public TrackerImpl(Class<T> type) {
            this.type = type;
        }

        @Override
        public @NotNull List<T> collect() {
            incomingTrackers.remove(this);
            return List.copyOf(packets);
        }
    }
}
