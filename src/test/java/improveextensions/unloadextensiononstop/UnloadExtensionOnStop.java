package improveextensions.unloadextensiononstop;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.type.monster.EntityZombie;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicBoolean;

public class UnloadExtensionOnStop extends Extension {

    private boolean terminated = false;

    @Override
    public void initialize() {
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            Assertions.assertTrue(terminated, "Extension should have been terminated on shutdown.");
            System.out.println("All tests passed.");
        }).makeTransient().schedule();

        MinecraftServer.getSchedulerManager().buildTask(MinecraftServer::stopCleanly).makeTransient().delay(1L, TimeUnit.SECOND).schedule();
    }

    @Override
    public void terminate() {
        terminated = true;
    }
}
