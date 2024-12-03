package net.minestom.testing;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public interface TestConnection {
    @NotNull Player connect(@NotNull Instance instance, @NotNull Pos pos);

    <T extends ServerPacket> @NotNull Collector<T> trackIncoming(@NotNull Class<T> type);

    default @NotNull Collector<ServerPacket> trackIncoming() {
        return trackIncoming(ServerPacket.class);
    }
}
