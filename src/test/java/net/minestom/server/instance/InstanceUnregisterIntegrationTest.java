package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.UUID;

import static net.minestom.testing.TestUtils.waitUntilCleared;

@EnvTest
public class InstanceUnregisterIntegrationTest {

    @Test
    public void sharedInstance(Env env) {
        // Ensure that unregistering a shared instance does not unload the container chunks
        var instanceManager = env.process().instance();
        var instance = instanceManager.createInstanceContainer();
        var shared1 = instanceManager.createSharedInstance(instance);
        var connection = env.createConnection();
        var player = connection.connect(shared1, new Pos(0, 40, 0));

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
    public void instanceNodeGC(Env env) {
        final class Game {
            final Instance instance;

            Game(Env env) {
                instance = env.process().instance().createInstanceContainer();
                instance.eventNode().addListener(PlayerMoveEvent.class, e -> System.out.println(instance));
            }
        }
        var game = new Game(env);
        var ref = new WeakReference<>(game);
        env.process().instance().unregisterInstance(game.instance);

        //noinspection UnusedAssignment
        game = null;
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

    @Test
    public void testGCWithEventsLambda(Env env) {
        var ref = new WeakReference<>(new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD));
        env.process().instance().registerInstance(ref.get());

        tmp(ref.get());

        ref.get().tick(0);
        env.process().instance().unregisterInstance(ref.get());

        waitUntilCleared(ref);
    }

    private void tmp(InstanceContainer instanceContainer) {
        instanceContainer.eventNode().addListener(InstanceTickEvent.class, (e) -> {
            var uuid = instanceContainer.getUniqueId();
        });
    }
}
