package net.minestom.server.thread;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class AcquirableLocalsIntegrationTest {
    @Test
    public void empty(Env env) {
        assertEquals(0, Acquirable.locals().count());
    }

    @Test
    public void localTest(Env env) {
        var instance = env.createFlatInstance();
        var zombie = new Entity(EntityType.ZOMBIE) {
            @Override
            public void tick(long time) {
                super.tick(time);
                assertEquals(Set.of(this), Acquirable.locals().collect(Collectors.toUnmodifiableSet()));
            }
        };
        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        env.tick();
    }
}
