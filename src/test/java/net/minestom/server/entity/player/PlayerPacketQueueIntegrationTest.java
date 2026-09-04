package net.minestom.server.entity.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class PlayerPacketQueueIntegrationTest {

    @Test
    void throwingHandlerReportsAndContinues(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        List<Throwable> reported = new ArrayList<>();
        env.process().exception().setExceptionHandler(reported::add);
        AtomicInteger handled = new AtomicInteger();
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientPluginMessagePacket.class,
                (_, _) -> {
                    if (handled.getAndIncrement() == 0) throw new IllegalStateException("handler failure");
                });

        player.addPacketToQueue(new ClientPluginMessagePacket("minestom:test", new byte[0]));
        player.addPacketToQueue(new ClientPluginMessagePacket("minestom:test", new byte[0]));
        assertDoesNotThrow(player::interpretPacketQueue);

        // Play state is above the default suppression level, the failure is reported
        assertEquals(1, reported.size());
        assertInstanceOf(IllegalStateException.class, reported.getFirst());
        // The queue keeps draining and the player stays connected by default
        assertEquals(2, handled.get());
        assertTrue(player.getPlayerConnection().isOnline());
    }
}
