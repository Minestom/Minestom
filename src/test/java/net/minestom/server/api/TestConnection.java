package net.minestom.server.api;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface TestConnection {
    CompletableFuture<Player> connect(Instance instance);

    <T extends ServerPacket> PacketTracker<T> trackIncoming(Class<T> type, Predicate<T> predicate, @Nullable Duration timeout);

    default <T extends ServerPacket> PacketTracker<T> trackIncoming(Class<T> type, Predicate<T> predicate) {
        return trackIncoming(type, predicate, null);
    }

    default <T extends ServerPacket> PacketTracker<T> trackIncoming(Class<T> type) {
        return trackIncoming(type, t -> true);
    }

    interface PacketTracker<T> {
        List<T> collect();
    }
}
