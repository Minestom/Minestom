package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvParameterResolver;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EnvParameterResolver.class)
public class EntityViewTest {

    @Test
    public void emptyEntity(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 42)).join();
        assertEquals(0, entity.getViewers().size());
    }

    @Test
    public void emptyPlayer(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(0, player.getViewers().size());
    }

    @Test
    public void multiPlayers(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 42));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 42));

        assertEquals(1, p1.getViewers().size());
        p1.getViewers().forEach(p -> assertEquals(p2, p));

        assertEquals(1, p2.getViewers().size());
        p2.getViewers().forEach(p -> assertEquals(p1, p));

        p2.remove();
        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        var p3 = env.createPlayer(instance, new Pos(0, 42, 42));
        assertEquals(1, p1.getViewers().size());
        p1.getViewers().forEach(p -> assertEquals(p3, p));
    }
}
