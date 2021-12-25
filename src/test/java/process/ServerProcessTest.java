package process;

import net.minestom.server.ServerProcess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerProcessTest {

    @Test
    public void init() {
        Assertions.assertDoesNotThrow(ServerProcess::newProcess);
    }

    @Test
    public void tick() throws Exception {
        var process = ServerProcess.newProcess();
        var ticker = process.ticker();
        Assertions.assertDoesNotThrow(() -> ticker.tick(System.currentTimeMillis()));
    }
}
