package process;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServerProcessTest {

    @Test
    public void init() {
        AtomicReference<ServerProcess> process = new AtomicReference<>();
        assertDoesNotThrow(() -> process.set(MinecraftServer.updateProcess()));
        assertDoesNotThrow(() -> process.get().start(new InetSocketAddress("localhost", 25565)));
        assertThrows(Exception.class, () -> process.get().start(new InetSocketAddress("localhost", 25566)));
        assertDoesNotThrow(() -> process.get().stop());
        assertThrows(Exception.class, () -> process.get().stop());
    }

    @Test
    public void tick() {
        var process = MinecraftServer.updateProcess();
        process.start(new InetSocketAddress("localhost", 25567));
        var ticker = process.ticker();
        assertDoesNotThrow(() -> ticker.tick(System.currentTimeMillis()));
        assertDoesNotThrow(process::stop);
    }
}
