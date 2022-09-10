package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityInstanceIntegrationTest {

    @Test
    public void entityJoin(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());
        assertEquals(new Pos(0, 42, 0), entity.getPosition());
    }

    @Test
    public void playerJoin(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());
        assertEquals(new Pos(0, 42, 0), player.getPosition());
    }

    @Test
    public void playerSwitch(Env env) {
        var instance = env.createFlatInstance();
        var instance2 = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());
        // #join may cause the thread to hang as scheduled for the next tick when initially in a pool
        assertTimeout(Duration.ofSeconds(2), () -> player.setInstance(instance2).join());
        assertEquals(instance2, player.getInstance());
    }

    @Test
    public void entitySwitchCancel(Env env) {
        var instance = env.createFlatInstance();
        var instance2 = env.createFlatInstance();
        var entity = new Entity(EntityType.BAT);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());
        env.process().eventHandler().addListener(AddEntityToInstanceEvent.class, event -> event.setCancelled(true));
        AtomicBoolean failed = new AtomicBoolean(false);
        var future = entity.setInstance(instance2).whenComplete((result, error) -> {
            assertTrue(error instanceof Exception, "error is not an exception");
            failed.set(true);
        });
        env.tickWhile(() -> !future.isDone(), Duration.ofSeconds(2));
        assertTrue(failed.get(), "future is not failed");
    }
}
