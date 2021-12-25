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
    public void start() throws Exception {
        var process = ServerProcess.newProcess();
    }
}
