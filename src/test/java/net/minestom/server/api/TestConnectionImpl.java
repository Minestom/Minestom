package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class TestConnectionImpl implements TestConnection {
    private final Env env;
    private final ServerProcess process;
    private final PlayerConnectionImpl playerConnection = new PlayerConnectionImpl();

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

    final class PlayerConnectionImpl extends PlayerConnection {
        @Override
        public void sendPacket(@NotNull SendablePacket packet) {

        }

        @Override
        public @NotNull SocketAddress getRemoteAddress() {
            return new InetSocketAddress("localhost", 25565);
        }

        @Override
        public void disconnect() {

        }
    }
}
