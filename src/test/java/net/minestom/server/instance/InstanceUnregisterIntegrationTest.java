package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerTickEvent;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;

@EnvTest
public class InstanceUnregisterIntegrationTest {

    @Test
    public void sharedInstance(Env env) {
        // Ensure that unregistering a shared instance does not unload the container chunks
        var instanceManager = env.process().instance();
        var instance = instanceManager.createInstanceContainer();
        var shared1 = instanceManager.createSharedInstance(instance);
        var connection = env.createConnection();
        var player = connection.connect(shared1, new Pos(0, 40, 0)).join();

        var listener = env.listen(PlayerTickEvent.class);
        listener.followup();
        env.tick();

        player.setInstance(instanceManager.createSharedInstance(instance)).join();
        listener.followup();
        env.tick();

        instanceManager.unregisterInstance(shared1);
        listener.followup();
        env.tick();
    }

    @Test
    public void instanceGC(Env env) {
        var instance = env.createFlatInstance();
        var ref = new WeakReference<>(instance);
        env.process().instance().unregisterInstance(instance);

        //noinspection UnusedAssignment
        instance = null;
        waitUntilCleared(ref);
    }

    @Test
    public void chunkGC(Env env) {
        // Ensure that unregistering an instance does release its chunks
        var instance = env.createFlatInstance();
        var chunk = instance.loadChunk(0, 0).join();
        var ref = new WeakReference<>(chunk);
        instance.unloadChunk(chunk);
        env.process().instance().unregisterInstance(instance);
        env.tick(); // Required to remove the chunk from the thread dispatcher

        //noinspection UnusedAssignment
        chunk = null;
        waitUntilCleared(ref);
    }

    private static void waitUntilCleared(WeakReference<?> ref) {
        while (ref.get() != null) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignore) {
            }
        }
    }
}
