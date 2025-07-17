package net.minestom.server;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ServerProcessTest {

    @Test
    public void init() {
        // These like to fail on github actions
        assumeTrue(System.getenv("GITHUB_ACTIONS") == null);

        AtomicReference<ServerProcess> process = new AtomicReference<>();
        assertDoesNotThrow(() -> process.set(MinecraftServer.updateProcess()));
        assertDoesNotThrow(() -> process.get().start(new InetSocketAddress("localhost", 25565)));
        assertThrows(Exception.class, () -> process.get().start(new InetSocketAddress("localhost", 25566)));
        assertDoesNotThrow(() -> process.get().stop());
    }

    @Test
    public void tick() {
        // These like to fail on github actions
        assumeTrue(System.getenv("GITHUB_ACTIONS") == null);

        var process = MinecraftServer.updateProcess();
        process.start(new InetSocketAddress("localhost", 25565));
        var ticker = process.ticker();
        assertDoesNotThrow(() -> ticker.tick(System.nanoTime()));
        assertDoesNotThrow(process::stop);
    }
}
