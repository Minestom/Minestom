package net.minestom.server.api;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface TestConnection {
    @NotNull CompletableFuture<@NotNull Player> connect(@NotNull Instance instance, @NotNull Pos pos, @NotNull Consumer<Player> loginCallback);

    default @NotNull CompletableFuture<@NotNull Player> connect(@NotNull Instance instance, @NotNull Pos pos) {
        return connect(instance, pos, (player) -> {
        });
    }

    <T extends ServerPacket> @NotNull Collector<T> trackIncoming(@NotNull Class<T> type);

    default @NotNull Collector<ServerPacket> trackIncoming() {
        return trackIncoming(ServerPacket.class);
    }
}
