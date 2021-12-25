package process;

import net.minestom.server.ServerProcess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class ServerProcessTest {

    @Test
    public void init() {
        AtomicReference<ServerProcess> process = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> process.set(ServerProcess.newProcess()));
        Assertions.assertDoesNotThrow(() -> process.get().start(new InetSocketAddress("localhost", 25565)));
    }

    @Test
    public void tick() throws Exception {
        var process = ServerProcess.newProcess();
        process.start(new InetSocketAddress("localhost", 25565));
        var ticker = process.ticker();
        Assertions.assertDoesNotThrow(() -> ticker.tick(System.currentTimeMillis()));
    }
}
